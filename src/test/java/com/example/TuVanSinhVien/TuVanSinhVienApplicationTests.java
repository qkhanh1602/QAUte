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
class TuVanSinhVienApplicationTests {

	@LocalServerPort
	private int port;
	@Autowired private TaiKhoanRepository accountRepository;
	@Autowired private YeuCauTuVanRepository ticketRepository;
	@Autowired private BaiVietHuongDanRepository articleRepository;
	@Autowired private SinhVienRepository studentRepository;
	@Autowired private NguoiTuVanRepository advisorRepository;
	@Autowired private JwtService jwtService;

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
		assertTrue(response.body().contains("data-view-engine=\"jsp-jstl\""));
		assertTrue(response.body().contains("data-layout-engine=\"sitemesh\""));
		assertTrue(response.body().contains("qaute-content-view"));
		assertFalse(response.body().contains("<%@"));
		assertFalse(response.body().contains("<sitemesh:write"));
	}

	@Test
	void allPublicJspViewsRenderSuccessfully() throws Exception {
		HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
		for (String path : new String[]{"/", "/faq", "/articles", "/advisors", "/login", "/register", "/advisor-register"}) {
			HttpResponse<String> response = client.send(
					HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build(),
					HttpResponse.BodyHandlers.ofString());
			assertEquals(200, response.statusCode(), "Không render được JSP " + path);
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
				"/admin/users", "/admin/catalogs", "/admin/content"});
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
		assertTrue(response.body().contains("qaute-content-view"), "Thiếu JSP/JSTL tại " + path);
		assertTrue(response.body().contains("data-layout-engine=\"sitemesh\""), "Thiếu SiteMesh tại " + path);
	}

}
