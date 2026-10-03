package com.example.TuVanSinhVien;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
		properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
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

    private org.springframework.messaging.simp.stomp.StompFrameHandler chatFrameHandler(java.util.concurrent.BlockingQueue<String> inbox) {
        return new org.springframework.messaging.simp.stomp.StompFrameHandler() {
            public java.lang.reflect.Type getPayloadType(org.springframework.messaging.simp.stomp.StompHeaders headers) { return String.class; }
            public void handleFrame(org.springframework.messaging.simp.stomp.StompHeaders headers, Object payload) { inbox.add((String) payload); }
        };
    }

}
