package com.example.TuVanSinhVien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import com.example.TuVanSinhVien.repository.BaiVietHuongDanRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
import com.example.TuVanSinhVien.service.JwtService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=", "app.workflow.scheduling.enabled=false"})
@org.springframework.context.annotation.Import(ReviewFixedClock.class)
@org.springframework.test.context.ActiveProfiles("dev")
class TuVanSinhVienApplicationTests {

	@LocalServerPort
	private int port;
	@Autowired private TaiKhoanRepository accountRepository;
	@Autowired private YeuCauTuVanRepository ticketRepository;
	@Autowired private BaiVietHuongDanRepository articleRepository;
	@Autowired private SinhVienRepository studentRepository;
	@Autowired private NguoiTuVanRepository advisorRepository;
	@Autowired private JwtService jwtService;
	@Autowired private com.example.TuVanSinhVien.config.FacultyAdvisorSeeder facultySeeder;
	@Autowired private com.example.TuVanSinhVien.repository.DonViRepository unitRepository;
	@Autowired private com.example.TuVanSinhVien.service.CatalogService catalogService;

    @Test
    void facultySeedIsIdempotentAndCatalogContainsOnlyEligibleAdvisors() {
        long count = accountRepository.count();
        facultySeeder.run();
        facultySeeder.run();
        assertEquals(count, accountRepository.count());
        var available = catalogService.getAllActiveAdvisors();
        assertTrue(available.stream().allMatch(a -> a.getNhanTuVan()
                && "HoatDong".equals(a.getTaiKhoan().getTrangThai())
                && "CoVan".equals(a.getTaiKhoan().getVaiTro()) && a.getDonVi().getHoatDong()));
        for (var unit : unitRepository.findByHoatDongTrue()) {
            if ("Khoa".equals(unit.getLoaiDonVi())) {
                assertTrue(available.stream().anyMatch(a -> a.getDonVi().getMaDonVi().equals(unit.getMaDonVi())),
                        "Missing advisor for " + unit.getMaDonViCode());
            }
        }
    }

    private static final String TEST_JWT_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
    @Autowired private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    @Autowired private com.example.TuVanSinhVien.service.ProfileService profileService;

    private TaiKhoan jwtTestAccount() {
        String username = "jwt_" + java.util.UUID.randomUUID().toString().replace("-", "");
        return accountRepository.saveAndFlush(TaiKhoan.builder().tenDangNhap(username)
                .matKhau(passwordEncoder.encode("JwtTest@123"))
                .hoTen("Kiểm thử JWT").email(username + "@example.test")
                .vaiTro("SinhVien").trangThai("HoatDong").tokenVersion(0)
                .ngayTao(java.time.LocalDateTime.now()).ngayCapNhat(java.time.LocalDateTime.now()).build());
    }

    private HttpResponse<String> jwtRequest(String path, String method, String token, String body) throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path));
        if (token != null) request.header("Authorization", "Bearer " + token);
        if (body != null) request.header("Content-Type", "application/json");
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()
                .send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private void assertJwtUnauthorized(HttpResponse<String> response) {
        assertEquals(401, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertFalse(response.headers().firstValue("Location").isPresent());
    }

    @Test
    void jwtTokenEndpointAndStatelessLogoutRevokeTokens() throws Exception {
        var account = jwtTestAccount();
        try {
            var invalid = jwtRequest("/api/auth/token", "POST", null,
                    "{\"username\":\"" + account.getTenDangNhap() + "\",\"password\":\"wrong\"}");
            assertJwtUnauthorized(invalid);
            var issued = jwtRequest("/api/auth/token", "POST", null,
                    "{\"username\":\"" + account.getTenDangNhap() + "\",\"password\":\"JwtTest@123\"}");
            assertEquals(200, issued.statusCode());
            assertFalse(issued.headers().allValues("Set-Cookie").stream().anyMatch(c -> c.startsWith("JSESSIONID=")));
            String token = new com.fasterxml.jackson.databind.ObjectMapper().readTree(issued.body()).get("accessToken").asText();
            var authenticated = jwtRequest("/api/notifications/unread-count", "GET", token, null);
            assertEquals(200, authenticated.statusCode());
            assertFalse(authenticated.headers().allValues("Set-Cookie").stream().anyMatch(c -> c.startsWith("JSESSIONID=")));
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", null, null));
            var forbidden = jwtRequest("/admin/dashboard", "GET", token, null);
            assertEquals(403, forbidden.statusCode());
            assertTrue(forbidden.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertEquals(200, jwtRequest("/api/auth/logout", "POST", token, null).statusCode());
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", token, null));
            assertJwtUnauthorized(jwtRequest("/api/auth/logout", "POST", token, null));
            assertEquals(1, accountRepository.findById(account.getMaTK()).orElseThrow().getTokenVersion());
        } finally { accountRepository.deleteById(account.getMaTK()); }
    }

    @Test
    void expiredTamperedLockedAndChangedRoleTokensCannotAuthenticate() throws Exception {
        var account = jwtTestAccount();
        try {
            String token = jwtService.createAccessToken(account);
            String expired = new com.example.TuVanSinhVien.service.JwtService(TEST_JWT_KEY, -1).createAccessToken(account);
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", expired, null));
            int signatureStart = token.lastIndexOf('.') + 1;
            String tampered = token.substring(0, signatureStart) + (token.charAt(signatureStart) == 'A' ? "B" : "A") + token.substring(signatureStart + 1);
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", tampered, null));
            account.setTrangThai("Khoa");
            account = accountRepository.saveAndFlush(account);
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", token, null));
            account.setTrangThai("HoatDong"); account.setVaiTro("Admin");
            account = accountRepository.saveAndFlush(account);
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", token, null));
            account.setVaiTro("SinhVien"); account = accountRepository.saveAndFlush(account);
            profileService.changePassword(account.getMaTK(), "JwtTest@123", "JwtChanged@456");
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", token, null));
            var changed = jwtRequest("/api/auth/token", "POST", null,
                    "{\"username\":\"" + account.getTenDangNhap() + "\",\"password\":\"JwtChanged@456\"}");
            assertEquals(200, changed.statusCode());
        } finally { accountRepository.deleteById(account.getMaTK()); }
    }

    @Test
    void browserCookieCannotBypassInvalidBearerAndWebLogoutRevokesJwt() throws Exception {
        var account = jwtTestAccount();
        try {
            String token = jwtService.createAccessToken(account);
            var cookies = new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
            var client = HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
            String base = "http://127.0.0.1:" + port;
            var login = client.send(HttpRequest.newBuilder(URI.create(base + "/login")).GET().build(), HttpResponse.BodyHandlers.ofString());
            var csrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
            assertTrue(csrf.find());
            var signedIn = client.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("username=" + account.getTenDangNhap() + "&password=JwtTest%40123&_csrf=" + csrf.group(1))).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(302, signedIn.statusCode());
            assertEquals(200, client.send(HttpRequest.newBuilder(URI.create(base + "/api/notifications/unread-count")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            var csrfDenied = client.send(HttpRequest.newBuilder(URI.create(base + "/api/notifications/1/read"))
                    .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(403, csrfDenied.statusCode());
            assertTrue(csrfDenied.headers().firstValue("Content-Type").orElse("").contains("application/json"));
            assertJwtUnauthorized(client.send(HttpRequest.newBuilder(URI.create(base + "/api/notifications/unread-count"))
                    .header("Authorization", "Bearer invalid").GET().build(), HttpResponse.BodyHandlers.ofString()));
            assertEquals(200, client.send(HttpRequest.newBuilder(URI.create(base + "/api/notifications/unread-count")).GET().build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            var confirm = client.send(HttpRequest.newBuilder(URI.create(base + "/logout")).GET().build(), HttpResponse.BodyHandlers.ofString());
            var logoutCsrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(confirm.body());
            assertTrue(logoutCsrf.find());
            assertEquals(302, client.send(HttpRequest.newBuilder(URI.create(base + "/logout"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("_csrf=" + logoutCsrf.group(1))).build(), HttpResponse.BodyHandlers.ofString()).statusCode());
            assertJwtUnauthorized(jwtRequest("/api/notifications/unread-count", "GET", token, null));
        } finally { accountRepository.deleteById(account.getMaTK()); }
    }

	@Test
	void contextLoads() {
	}

	@Test
	void jspJstlDecoratorIsAppliedBySiteMesh() throws Exception {
		HttpClient client = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/login"))
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.headers().firstValue("Content-Type").orElse("").toLowerCase().contains("charset=utf-8"));
		assertTrue(response.body().contains("Đăng nhập"));
		assertTrue(response.body().contains("data-view-engine=\"jsp-jstl\""));
		assertTrue(response.body().contains("data-layout-engine=\"sitemesh\""));
		assertTrue(response.body().contains("qaute-content-view"));
		assertFalse(response.body().contains("<%@"));
		assertFalse(response.body().contains("<sitemesh:write"));
	}

    @Test
    void expiredChatSessionReturnsJsonAndAdvisorSelfRegistrationIsRemoved() throws Exception {
        var response = get("/api/chat/1/messages", null);
        assertEquals(401, response.statusCode());
        assertTrue(response.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        assertTrue(response.body().contains("Vui lòng đăng nhập lại"));
        assertFalse(response.body().contains("<!DOCTYPE"));
        var registration = get("/register", null);
        assertEquals(200, registration.statusCode());
        assertTrue(registration.body().contains("Tài khoản cố vấn do nhà trường cấp"));
        assertFalse(registration.body().contains("/advisor-register"));
        var removed = get("/advisor-register", activeAccount("Admin"));
        assertTrue(removed.statusCode() >= 300, "Advisor self registration must be unavailable even to an authenticated user");
    }

	@Test
	void allPublicJspViewsRenderSuccessfully() throws Exception {
		HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
		for (String path : new String[]{"/", "/faq", "/articles", "/advisors", "/login", "/register"}) {
			HttpResponse<String> response = client.send(
					HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Không render được JSP " + path);


            assertTrue(response.headers().firstValue("Content-Type").orElse("").toLowerCase().contains("charset=utf-8"), path);
            assertTrue(response.body().contains(java.util.Set.of("/register").contains(path)?"Đăng ký":"Trang chủ"), "Vietnamese encoding " + path);
            if (!java.util.Set.of("/login","/register").contains(path))
                assertTrue(response.body().contains("Hệ thống tư vấn sinh viên"), "Footer encoding " + path);
            assertFalse(response.body().contains("Ã"), "Mojibake " + path);
			assertTrue(response.body().contains("qaute-content-view"), "Thiếu dấu vết JSP/JSTL tại " + path);
			assertTrue(response.body().contains("data-layout-engine=\"sitemesh\""), "Thiếu SiteMesh tại " + path);
			assertFalse(response.body().matches("(?s).*\\s+th:[a-zA-Z-]+=.*"), "Còn thuộc tính Thymeleaf tại " + path);
		}
	}

	@Test
	void authenticatedRoleJspViewsRenderSuccessfully() throws Exception {
		assertRoleViews("SinhVien", new String[]{"/profile", "/student/dashboard", "/student/tickets",
				"/student/tickets/new", "/student/appointments", "/student/chat", "/student/history"});
		assertRoleViews("CoVan", new String[]{"/profile", "/advisor/dashboard", "/advisor/tickets",
				"/advisor/slots", "/advisor/appointments", "/advisor/chat"});
		assertRoleViews("Admin", new String[]{"/profile", "/admin/dashboard", "/admin/tickets",
				"/admin/users", "/admin/catalogs", "/admin/content", "/admin/search", "/admin/search?type=appointments", "/admin/search?type=logs"});
	}

	@Test
	void detailJspViewsRenderSuccessfully() throws Exception {
		articleRepository.findByTrangThaiOrderByNgayDangDesc("XuatBan").stream().findFirst().ifPresent(article -> {
			try { assertRendered(get("/articles/" + article.getMaBaiViet(), null), "/articles/{id}"); }
			catch (Exception ex) { throw new RuntimeException(ex); }
		});
		TaiKhoan studentAccount = activeAccount("SinhVien");
		Integer studentId = studentRepository.findByTaiKhoan_MaTK(studentAccount.getMaTK()).orElseThrow().getMaSV();
		YeuCauTuVan studentTicket = ticketRepository.findBySinhVien_MaSVOrderByNgayTaoDesc(studentId).stream().findFirst().orElseThrow();
		assertRendered(get("/student/tickets/" + studentTicket.getMaYeuCau(), studentAccount), "/student/tickets/{id}");
		TaiKhoan advisorAccount = accountRepository.findByVaiTro("CoVan").stream()
				.filter(item -> "HoatDong".equals(item.getTrangThai()))
				.filter(item -> advisorRepository.findByTaiKhoan_MaTK(item.getMaTK())
						.map(profile -> !ticketRepository.findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(profile.getMaCV()).isEmpty())
						.orElse(false))
				.findFirst().orElseThrow();
		Integer advisorId = advisorRepository.findByTaiKhoan_MaTK(advisorAccount.getMaTK()).orElseThrow().getMaCV();
		YeuCauTuVan advisorTicket = ticketRepository.findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(advisorId).getFirst();
		assertRendered(get("/advisor/tickets/" + advisorTicket.getMaYeuCau(), advisorAccount), "/advisor/tickets/{id}");
	}

	private void assertRoleViews(String role, String[] paths) throws Exception {
		TaiKhoan account = activeAccount(role);
		for (String path : paths) assertRendered(get(path, account), path);
	}

	private TaiKhoan activeAccount(String role) {
		return accountRepository.findByVaiTro(role).stream()
				.filter(item -> "HoatDong".equals(item.getTrangThai())).findFirst().orElseThrow();
	}

	private HttpResponse<String> get(String path, TaiKhoan account) throws Exception {
		HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET();
		if (account != null) request.header("Authorization", "Bearer " + jwtService.createAccessToken(account));
		return HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build()
				.send(request.build(), HttpResponse.BodyHandlers.ofString());
	}

	private void assertRendered(HttpResponse<String> response, String path) {
		assertEquals(200, response.statusCode(), "Không render được JSP " + path);
        if (path.startsWith("/advisor/chat")) {
            var html = org.jsoup.Jsoup.parse(response.body());
            assertEquals(1, html.select(".advisor-chat-layout > .advisor-chat-session-list").size());
            assertEquals(1, html.select(".advisor-chat-layout > .advisor-chat-conversation").size(),
                    "Conversation must be a sibling of the queue, not inside its narrow column");
            assertEquals(0, html.select(".advisor-chat-session-list .advisor-chat-conversation").size());
        }
            assertTrue(response.headers().firstValue("Content-Type").orElse("").toLowerCase().contains("charset=utf-8"), path);
            assertTrue(response.body().contains(java.util.Set.of("/register").contains(path)?"Đăng ký":"Trang chủ"), "Vietnamese encoding " + path);
            if (!java.util.Set.of("/login","/register").contains(path))
                assertTrue(response.body().contains("Hệ thống tư vấn sinh viên"), "Footer encoding " + path);
            assertFalse(response.body().contains("Ã"), "Mojibake " + path);
		assertTrue(response.body().contains("qaute-content-view"), "Thiếu JSP/JSTL tại " + path);
		assertTrue(response.body().contains("data-layout-engine=\"sitemesh\""), "Thiếu SiteMesh tại " + path);
	}


    @Test
    void anonymousHomeAndAdminFormLoginUseCorrectPages() throws Exception {
        var cookies = new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();
        String base = "http://127.0.0.1:" + port;
        var home = client.send(HttpRequest.newBuilder(URI.create(base + "/")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertRendered(home, "/");
        assertFalse(home.headers().firstValue("Location").isPresent());
        var login = client.send(HttpRequest.newBuilder(URI.create(base + "/login")).GET().build(), HttpResponse.BodyHandlers.ofString());
        var csrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
        assertTrue(csrf.find(), "Login form must contain CSRF token");
        String body = "username=admin&password=" + java.net.URLEncoder.encode("Admin@123",java.nio.charset.StandardCharsets.UTF_8)
                + "&_csrf=" + java.net.URLEncoder.encode(csrf.group(1),java.nio.charset.StandardCharsets.UTF_8);
        var authenticated = client.send(HttpRequest.newBuilder(URI.create(base + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, authenticated.statusCode());
        assertTrue(authenticated.headers().firstValue("Location").orElse("").endsWith("/admin/dashboard"));
        var dashboard = client.send(HttpRequest.newBuilder(URI.create(base + "/admin/dashboard")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertRendered(dashboard,"/admin/dashboard with session cookie");
        var publicHome = client.send(HttpRequest.newBuilder(URI.create(base + "/")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertRendered(publicHome,"/ after login");
        var confirmation = client.send(HttpRequest.newBuilder(URI.create(base + "/logout")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertRendered(confirmation, "GET logout confirmation");
        assertTrue(confirmation.body().contains("Đăng xuất khỏi QAUTE?"));
        var stillLoggedIn = client.send(HttpRequest.newBuilder(URI.create(base + "/admin/dashboard")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, stillLoggedIn.statusCode(), "GET must not log out the session");
        var blocked = client.send(HttpRequest.newBuilder(URI.create(base + "/logout"))
                .POST(HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(403, blocked.statusCode(), "Logout requires CSRF");
        var logoutCsrf = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(confirmation.body());
        assertTrue(logoutCsrf.find());
        var loggedOut = client.send(HttpRequest.newBuilder(URI.create(base + "/logout"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("_csrf=" + java.net.URLEncoder.encode(logoutCsrf.group(1), java.nio.charset.StandardCharsets.UTF_8)))
                .build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, loggedOut.statusCode());
        assertTrue(loggedOut.headers().firstValue("Location").orElse("").endsWith("/?logout=true"));
        var protectedPage = client.send(HttpRequest.newBuilder(URI.create(base + "/admin/dashboard")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, protectedPage.statusCode());
        assertTrue(protectedPage.headers().firstValue("Location").orElse("").endsWith("/login"));
    }
    @Autowired private com.example.TuVanSinhVien.repository.PhienChatRepository chatSessionRepository;
    @Autowired private org.springframework.transaction.PlatformTransactionManager transactionManager;
    @Autowired private jakarta.persistence.EntityManager entityManager;

    @Test
    void websocketChatDeliversCommittedMessagesToBothParticipantsWithoutReload() throws Exception {
        var tx = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        var studentAccount = accountRepository.findByTenDangNhap("sinhvien1").orElseThrow();
        var advisorAccount = accountRepository.findByTenDangNhap("covan.cntt").orElseThrow();
        Integer sessionId = tx.execute(status -> {
            var student = studentRepository.findByTaiKhoan(studentAccount).orElseThrow();
            var advisor = advisorRepository.findByTaiKhoan(advisorAccount).orElseThrow();
            return chatSessionRepository.save(com.example.TuVanSinhVien.entity.PhienChat.builder()
                    .sinhVien(student).nguoiTuVan(advisor).donVi(advisor.getDonVi())
                    .trangThai("DangHoatDong").build()).getMaPhienChat();
        });
        var transport = new org.springframework.web.socket.sockjs.client.WebSocketTransport(
                new org.springframework.web.socket.client.standard.StandardWebSocketClient());
        var client = new org.springframework.web.socket.messaging.WebSocketStompClient(
                new org.springframework.web.socket.sockjs.client.SockJsClient(java.util.List.of(transport)));
        client.setMessageConverter(new org.springframework.messaging.converter.MessageConverter() {
            public Object fromMessage(org.springframework.messaging.Message<?> message, Class<?> type) {
                return new String((byte[]) message.getPayload(), java.nio.charset.StandardCharsets.UTF_8);
            }
            public org.springframework.messaging.Message<?> toMessage(Object payload, org.springframework.messaging.MessageHeaders headers) {
                return new org.springframework.messaging.support.GenericMessage<>(payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8), headers);
            }
        });
        org.springframework.messaging.simp.stomp.StompSession studentSocket = null, advisorSocket = null;
        var studentInbox = new java.util.concurrent.LinkedBlockingQueue<String>();
        var advisorInbox = new java.util.concurrent.LinkedBlockingQueue<String>();
        try {
            var studentHeaders = new org.springframework.web.socket.WebSocketHttpHeaders();
            studentHeaders.setBearerAuth(jwtService.createAccessToken(studentAccount));
            var advisorHeaders = new org.springframework.web.socket.WebSocketHttpHeaders();
            advisorHeaders.setBearerAuth(jwtService.createAccessToken(advisorAccount));
            String endpoint = "ws://127.0.0.1:" + port + "/ws";
            studentSocket = client.connectAsync(endpoint, studentHeaders, new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {}).get(10, java.util.concurrent.TimeUnit.SECONDS);
            advisorSocket = client.connectAsync(endpoint, advisorHeaders, new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {}).get(10, java.util.concurrent.TimeUnit.SECONDS);
            studentSocket.subscribe("/user/queue/chat", chatFrameHandler(studentInbox));
            advisorSocket.subscribe("/user/queue/chat", chatFrameHandler(advisorInbox));
            Thread.sleep(200); // Allow both distinct broker connections to register subscriptions.
            var sendHeaders = new org.springframework.messaging.simp.stomp.StompHeaders();
            sendHeaders.setDestination("/app/chat.send");
            sendHeaders.setContentType(org.springframework.util.MimeTypeUtils.APPLICATION_JSON);
            studentSocket.send(sendHeaders, "{\"sessionId\":" + sessionId + ",\"content\":\"Kiểm thử chat trực tuyến\"}");
            String receivedByStudent = studentInbox.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            String receivedByAdvisor = advisorInbox.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            assertTrue(receivedByStudent != null && receivedByStudent.contains("Kiểm thử chat trực tuyến"));
            assertEquals(receivedByStudent, receivedByAdvisor);
            advisorSocket.send(sendHeaders, "{\"sessionId\":" + sessionId + ",\"content\":\"Cố vấn đã nhận được tin\"}");
            assertTrue(studentInbox.poll(10, java.util.concurrent.TimeUnit.SECONDS).contains("Cố vấn đã nhận được tin"));
            assertTrue(advisorInbox.poll(10, java.util.concurrent.TimeUnit.SECONDS).contains("Cố vấn đã nhận được tin"));
            var historyUri = URI.create("http://127.0.0.1:" + port + "/api/chat/" + sessionId + "/messages");
            var history = HttpClient.newHttpClient().send(HttpRequest.newBuilder(historyUri)
                    .header("Authorization", "Bearer " + jwtService.createAccessToken(studentAccount)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, history.statusCode());
            assertTrue(history.body().contains("Cố vấn đã nhận được tin"));
            var otherAccount = accountRepository.findByTenDangNhap("sinhvien2").orElseThrow();
            var forbiddenHistory = HttpClient.newHttpClient().send(HttpRequest.newBuilder(historyUri)
                    .header("Authorization", "Bearer " + jwtService.createAccessToken(otherAccount)).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(403, forbiddenHistory.statusCode(), "Other students cannot poll this chat");
        } finally {
            if (studentSocket != null && studentSocket.isConnected()) studentSocket.disconnect();
            if (advisorSocket != null && advisorSocket.isConnected()) advisorSocket.disconnect();
            client.stop();
            tx.executeWithoutResult(status -> {
                entityManager.createQuery("delete from TinNhanChat m where m.phienChat.maPhienChat=:id").setParameter("id", sessionId).executeUpdate();
                chatSessionRepository.deleteById(sessionId);
            });
        }
    }

    @Test
    void websocketRequiresLoginAndRejectsDirectBrokerSend() throws Exception {
        var info = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/ws/info")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertEquals(401,info.statusCode());
        var account=jwtTestAccount();
        var transport=new org.springframework.web.socket.sockjs.client.WebSocketTransport(new org.springframework.web.socket.client.standard.StandardWebSocketClient());
        var client=new org.springframework.web.socket.messaging.WebSocketStompClient(new org.springframework.web.socket.sockjs.client.SockJsClient(java.util.List.of(transport)));
        client.setMessageConverter(new org.springframework.messaging.converter.MessageConverter() {
            public Object fromMessage(org.springframework.messaging.Message<?> message,Class<?> type) {
                return new String((byte[])message.getPayload(),java.nio.charset.StandardCharsets.UTF_8);
            }
            public org.springframework.messaging.Message<?> toMessage(Object payload,org.springframework.messaging.MessageHeaders headers) {
                return new org.springframework.messaging.support.GenericMessage<>(payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),headers);
            }
        });
        var errors=new java.util.concurrent.CompletableFuture<String>();
        org.springframework.messaging.simp.stomp.StompSession socket=null;
        try {
            var headers=new org.springframework.web.socket.WebSocketHttpHeaders();headers.setBearerAuth(jwtService.createAccessToken(account));
            socket=client.connectAsync("ws://127.0.0.1:"+port+"/ws",headers,new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {
                @Override public java.lang.reflect.Type getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders h) { return String.class; }
                @Override public void handleFrame(org.springframework.messaging.simp.stomp.StompHeaders h,Object payload) { errors.complete(payload.toString()); }
                @Override public void handleTransportError(org.springframework.messaging.simp.stomp.StompSession s,Throwable error) { errors.complete("closed"); }
            }).get(10,java.util.concurrent.TimeUnit.SECONDS);
            socket.send("/queue/review-probe-"+java.util.UUID.randomUUID(),"forbidden");
            assertNotNull(errors.get(10,java.util.concurrent.TimeUnit.SECONDS));
        } finally {
            if(socket!=null && socket.isConnected()) socket.disconnect();client.stop();accountRepository.deleteById(account.getMaTK());
        }
    }

    @Test void temporaryPasswordTokenCanOnlyChangePasswordAndLogout() throws Exception {
        var account=jwtTestAccount();account.setBatBuocDoiMatKhau(true);account=accountRepository.saveAndFlush(account);
        try {
            String token=jwtService.createAccessToken(account);
            var blocked=jwtRequest("/api/chat/1/messages","GET",token,null);
            assertEquals(403,blocked.statusCode());assertTrue(blocked.body().contains("PASSWORD_CHANGE_REQUIRED"));
            var changed=jwtRequest("/api/auth/change-password","POST",token,"{\"oldPassword\":\"JwtTest@123\",\"newPassword\":\"NewJwtTest@456\"}");
            assertEquals(200,changed.statusCode());assertFalse(accountRepository.findById(account.getMaTK()).orElseThrow().getBatBuocDoiMatKhau());
            assertEquals(401,jwtRequest("/api/chat/1/messages","GET",token,null).statusCode());
        } finally {accountRepository.deleteById(account.getMaTK());}
    }

    @Autowired private com.example.TuVanSinhVien.service.AppointmentService appointmentService;
    @Autowired private org.springframework.jdbc.core.JdbcTemplate jdbc;
    @Autowired private com.example.TuVanSinhVien.service.SlaService officeCalendar;
    @Autowired private java.time.Clock clock;
    @Autowired private org.springframework.transaction.PlatformTransactionManager appointmentTransactions;

    @Test void appointmentChangeReachesAuthenticatedSocketAndFreshStudentView() throws Exception {
        var student=accountRepository.findAll().stream().filter(t->"SinhVien".equals(t.getVaiTro()) && "HoatDong".equals(t.getTrangThai()) && !Boolean.TRUE.equals(t.getBatBuocDoiMatKhau())).findFirst().orElseThrow();
        var advisor=catalogService.getAllActiveAdvisors().getFirst();
        var transport=new org.springframework.web.socket.sockjs.client.WebSocketTransport(new org.springframework.web.socket.client.standard.StandardWebSocketClient());
        var client=new org.springframework.web.socket.messaging.WebSocketStompClient(new org.springframework.web.socket.sockjs.client.SockJsClient(java.util.List.of(transport)));
        client.setMessageConverter(new org.springframework.messaging.converter.MessageConverter() {
            public Object fromMessage(org.springframework.messaging.Message<?> message,Class<?> type) {
                return new String((byte[])message.getPayload(),java.nio.charset.StandardCharsets.UTF_8);
            }
            public org.springframework.messaging.Message<?> toMessage(Object payload,org.springframework.messaging.MessageHeaders headers) {
                return new org.springframework.messaging.support.GenericMessage<>(payload.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),headers);
            }
        });
        var headers=new org.springframework.web.socket.WebSocketHttpHeaders(); headers.setBearerAuth(jwtService.createAccessToken(student));
        var socket=client.connectAsync("ws://127.0.0.1:"+port+"/ws",headers,new org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter() {}).get(10,java.util.concurrent.TimeUnit.SECONDS);
        var inbox=new java.util.concurrent.LinkedBlockingQueue<String>();
        Integer slotId=null;
        Integer cancelTestId=null;
        String cancelTitle="TEST_HTTP_CANCEL_"+java.util.UUID.randomUUID();
        try {
            socket.subscribe("/queue/appointments",chatFrameHandler(inbox));
            Thread.sleep(200);
            var start=java.time.LocalDate.now(clock).plusDays(1).atTime(14,0);
            while(!officeCalendar.isOfficeInterval(start,start.plusMinutes(45))) start=start.plusDays(1);
            slotId=appointmentService.createSlot(advisor,start,start.plusMinutes(45),"TrucTiep","TEST_SOCKET_LIVE_APPOINTMENT").getMaKhungGio();
            String notice=inbox.poll(10,java.util.concurrent.TimeUnit.SECONDS);
            assertNotNull(notice); assertTrue(notice.contains("changed"));
            assertFalse(notice.contains(student.getEmail()));
            var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/student/appointments?maCV="+advisor.getMaCV()+"&tab=book"))
                .header("Authorization","Bearer "+jwtService.createAccessToken(student)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,response.statusCode());
            assertTrue(response.body().contains("data-appointment-live-root"));
            assertTrue(response.body().contains("bookModal"+slotId));
            assertTrue(response.body().contains("data-office-calendar"));
            assertTrue(response.body().contains("data-office-start="));
            assertTrue(response.body().contains("/student/appointments/request"));
            assertTrue(response.body().contains("qaute-appointments.js"));
            assertFalse(response.body().contains("Dự án tốt nghiệp"));
            for(String path:java.util.List.of("/advisor/appointments","/advisor/slots")) {
                var advisorView=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path))
                    .header("Authorization","Bearer "+jwtService.createAccessToken(advisor.getTaiKhoan())).GET().build(),HttpResponse.BodyHandlers.ofString());
                assertEquals(200,advisorView.statusCode(),path);
                assertTrue(advisorView.body().contains("data-appointment-live-root"));
                assertTrue(advisorView.body().contains("qaute-appointments.js"));
            }
            final Integer createdSlot=slotId;
            cancelTestId=new org.springframework.transaction.support.TransactionTemplate(appointmentTransactions).execute(status->{
                var sv=studentRepository.findByTaiKhoan_MaTK(student.getMaTK()).orElseThrow();
                return appointmentService.bookAppointment(sv,createdSlot,cancelTitle,"Nội dung kiểm tra hủy lịch qua HTTP.").getMaLichHen();
            });
            assertNotNull(inbox.poll(10,java.util.concurrent.TimeUnit.SECONDS));
            var cancelClient=HttpClient.newBuilder().cookieHandler(new java.net.CookieManager(null,java.net.CookiePolicy.ACCEPT_ALL)).build();
            var cancelResponse=cancelClient.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/student/appointments/"+cancelTestId+"/cancel"))
                .header("Authorization","Bearer "+jwtService.createAccessToken(student))
                .header("Content-Type","application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("lyDo="+java.net.URLEncoder.encode("Trùng lịch học",java.nio.charset.StandardCharsets.UTF_8))).build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(302,cancelResponse.statusCode());
            assertEquals("Huy",jdbc.queryForObject("SELECT TrangThai FROM dbo.LichHenTuVan WHERE MaLichHen=?",String.class,cancelTestId));
            assertNotNull(inbox.poll(10,java.util.concurrent.TimeUnit.SECONDS),"Cancellation must notify live appointment pages");
            var fresh=cancelClient.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/student/appointments"))
                .header("Authorization","Bearer "+jwtService.createAccessToken(student)).GET().build(),HttpResponse.BodyHandlers.ofString());
            assertEquals(200,fresh.statusCode());
            assertTrue(fresh.body().contains(cancelTitle));assertTrue(fresh.body().contains("status-Huy"));
            assertTrue(fresh.body().contains("Đã hủy lịch hẹn tư vấn thành công."));
        } finally {
            socket.disconnect(); client.stop();
            if(cancelTestId!=null) jdbc.update("DELETE FROM dbo.LichHenTuVan WHERE MaLichHen=?",cancelTestId);
            jdbc.update("DELETE FROM dbo.ThongBao WHERE NoiDung LIKE ?","%"+cancelTitle+"%");
            if(slotId!=null) jdbc.update("DELETE FROM dbo.KhungGioTuVan WHERE MaKhungGio=?",slotId);
        }
    }

    private org.springframework.messaging.simp.stomp.StompFrameHandler chatFrameHandler(java.util.concurrent.BlockingQueue<String> inbox) {
        return new org.springframework.messaging.simp.stomp.StompFrameHandler() {
            public java.lang.reflect.Type getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders headers) { return String.class; }
            public void handleFrame(org.springframework.messaging.simp.stomp.StompHeaders headers, Object payload) { inbox.add((String) payload); }
        };
    }

}
