package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.workflow.scheduling.enabled=false"
        })
@org.springframework.context.annotation.Import(ReviewFixedClock.class)
@org.springframework.test.context.ActiveProfiles("dev")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AllFunctionalFlowsVerificationTest {

    @LocalServerPort
    private int port;

    @Autowired private TaiKhoanRepository accountRepo;
    @Autowired private SinhVienRepository studentRepo;
    @Autowired private NguoiTuVanRepository advisorRepo;
    @Autowired private DonViRepository unitRepo;
    @Autowired private LinhVucTuVanRepository fieldRepo;
    @Autowired private YeuCauTuVanRepository ticketRepo;
    @Autowired private TicketService ticketService;
    @Autowired private ForumService forumService;
    @Autowired private AppointmentService appointmentService;
    @Autowired private KhungGioTuVanRepository slotRepo;
    @Autowired private LichHenTuVanRepository appointmentRepo;
    @Autowired private ConsultationRecordPdfService recordPdfService;
    @Autowired private ReportExportService reportExportService;
    @Autowired private ReportService reportService;
    @Autowired private JwtService jwtService;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager txManager;

    private TransactionTemplate tx() {
        return new TransactionTemplate(txManager);
    }

    private String baseUrl() {
        return "http://127.0.0.1:" + port;
    }

    @Test
    @Order(1)
    @DisplayName("Luồng 1: Xác thực & Phân quyền (Form Login, CSRF, JWT, Role Access)")
    void testAuthenticationAndAuthorizationFlow() throws Exception {
        System.out.println("===> BẮT ĐẦU KIỂM TRA LUỒNG 1: XÁC THỰC & PHÂN QUYỀN <===");

        var cookies = new java.net.CookieManager(null, java.net.CookiePolicy.ACCEPT_ALL);
        var client = HttpClient.newBuilder().cookieHandler(cookies).followRedirects(HttpClient.Redirect.NEVER).build();

        // 1.1 Kiểm tra truy cập trang đăng nhập và lấy CSRF token
        var loginPage = client.send(HttpRequest.newBuilder(URI.create(baseUrl() + "/login")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginPage.statusCode(), "Trang đăng nhập phải trả về HTTP 200");
        var csrfMatcher = java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(loginPage.body());
        assertTrue(csrfMatcher.find(), "Form đăng nhập phải chứa CSRF token hợp lệ");
        String csrfToken = csrfMatcher.group(1);

        // 1.2 Đăng nhập sinh viên thành công
        String studentBody = "username=sinhvien1&password=Sinhvien%40123&_csrf=" + csrfToken;
        var studentLogin = client.send(HttpRequest.newBuilder(URI.create(baseUrl() + "/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(studentBody)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(302, studentLogin.statusCode(), "Đăng nhập sinh viên phải chuyển hướng (302)");
        assertTrue(studentLogin.headers().firstValue("Location").orElse("").endsWith("/student/dashboard"),
                "Sinh viên phải được chuyển hướng vào /student/dashboard");

        // 1.3 Kiểm tra phân quyền: Sinh viên không được vào trang /admin
        var forbiddenAdmin = client.send(HttpRequest.newBuilder(URI.create(baseUrl() + "/admin/dashboard")).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertTrue(forbiddenAdmin.statusCode() == 302 || forbiddenAdmin.statusCode() == 403,
                "Sinh viên không được phép truy cập trang admin");

        // 1.4 Lấy JWT token cho REST API
        var jwtResponse = client.send(HttpRequest.newBuilder(URI.create(baseUrl() + "/api/auth/token"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"username\":\"sinhvien1\",\"password\":\"Sinhvien@123\"}")).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, jwtResponse.statusCode(), "REST API cấp JWT token thành công");
        assertTrue(jwtResponse.body().contains("accessToken"), "Phản hồi API phải chứa accessToken");

        System.out.println("===> KẾT QUẢ LUỒNG 1: HOÀN TẤT THÀNH CÔNG! <===");
    }

    @Test
    @Order(2)
    @DisplayName("Luồng 2: Quản lý Phiếu Tư vấn (Tạo phiếu -> Tiếp nhận -> Trả lời -> Hỏi thêm -> Đóng phiếu -> Đánh giá)")
    void testTicketLifecycleFlow() {
        System.out.println("===> BẮT ĐẦU KIỂM TRA LUỒNG 2: VÒNG ĐỜI PHIẾU TƯ VẤN <===");

        SinhVien student = tx().execute(s -> {
            SinhVien st = studentRepo.findAll().stream().filter(item -> "sinhvien1".equals(item.getMssv()) || "sinhvien1".equals(item.getTaiKhoan().getTenDangNhap())).findFirst().orElseThrow();
            org.hibernate.Hibernate.initialize(st.getTaiKhoan());
            return st;
        });
        DonVi unit = unitRepo.findByMaDonViCode("CNTT").orElseThrow();
        LinhVucTuVan field = fieldRepo.findByDonVi_MaDonViAndTrangThai(unit.getMaDonVi(), "HoatDong").getFirst();
        NguoiTuVan advisor = tx().execute(s -> {
            NguoiTuVan adv = advisorRepo.findAllWithAccountAndUnit().stream().filter(a -> a.getDonVi().getMaDonVi().equals(unit.getMaDonVi()) && Boolean.TRUE.equals(a.getNhanTuVan())).findFirst().orElseThrow();
            org.hibernate.Hibernate.initialize(adv.getTaiKhoan());
            org.hibernate.Hibernate.initialize(adv.getDonVi());
            return adv;
        });

        String uniqueTitle = "TEST_FLOW_TICKET_" + UUID.randomUUID().toString().substring(0, 8);
        String content = "Nội dung câu hỏi kiểm tra toàn diện luồng tư vấn từ tạo đến kết thúc.";

        // 2.1 Sinh viên tạo phiếu tư vấn mới
        YeuCauTuVan ticket = ticketService.createTicket(student, field.getMaLinhVuc(), unit.getMaDonVi(), uniqueTitle, content, false);
        assertNotNull(ticket.getMaYeuCau());
        assertEquals("ChoTiepNhan", ticket.getTrangThai());
        assertNotNull(ticket.getHanTiepNhan(), "Phải tính hạn SLA tiếp nhận");
        assertNotNull(ticket.getHanSLA(), "Phải tính hạn SLA giải quyết");
        Integer ticketId = ticket.getMaYeuCau();

        try {
            // 2.2 Cố vấn tiếp nhận phiếu từ hàng đợi khoa
            ticketService.claimTicket(ticketId, advisor);
            YeuCauTuVan claimed = ticketRepo.findById(ticketId).orElseThrow();
            assertEquals("DangXuLy", claimed.getTrangThai(), "Trạng thái sau khi tiếp nhận phải là DangXuLy");
            assertEquals(advisor.getMaCV(), claimed.getCoVanPhuTrach().getMaCV(), "Cố vấn phụ trách phải khớp");

            // 2.3 Cố vấn trả lời phiếu tư vấn
            String replyContent = "Chào em, cố vấn đã ghi nhận và trả lời hướng dẫn chi tiết cho em.";
            ticketService.replyTicket(ticketId, advisor, replyContent, false);
            YeuCauTuVan replied = ticketRepo.findById(ticketId).orElseThrow();
            assertEquals("DaPhanHoi", replied.getTrangThai(), "Trạng thái sau phản hồi phải là DaPhanHoi");
            assertNotNull(replied.getHanTuDongDong(), "Phải có hạn tự động đóng sau 7 ngày");

            // 2.4 Sinh viên gửi câu hỏi làm rõ thêm (follow-up)
            ticketService.studentFollowUp(ticketId, student, "Dạ em cảm ơn thầy/cô, em muốn hỏi thêm chút về thời hạn đăng ký ạ?");
            YeuCauTuVan followedUp = ticketRepo.findById(ticketId).orElseThrow();
            assertEquals("DangXuLy", followedUp.getTrangThai(), "Sau khi sinh viên hỏi thêm, phiếu quay lại DangXuLy");

            // 2.5 Cố vấn phản hồi lần 2 và chốt câu hỏi
            ticketService.replyTicket(ticketId, advisor, "Thời hạn đến hết ngày 30 hàng tháng em nhé.", false);

            // 2.6 Sinh viên đóng phiếu chủ động
            ticketService.closeTicket(ticketId, student, "Đã hiểu rõ quy trình và thỏa mãn câu trả lời.");
            YeuCauTuVan closed = ticketRepo.findById(ticketId).orElseThrow();
            assertEquals("Dong", closed.getTrangThai(), "Trạng thái phiếu phải là Dong");
            assertNotNull(closed.getNgayDong());

            // 2.7 Sinh viên đánh giá chất lượng buổi tư vấn (5 sao)
            ticketService.rateTicket(ticketId, student.getMaSV(), (short) 5, "Cố vấn phản hồi rất nhiệt tình và nhanh chóng!");
            long ratingCount = jdbc.queryForObject("SELECT COUNT(*) FROM dbo.DanhGiaYeuCau WHERE MaYeuCau=?", Long.class, ticketId);
            assertEquals(1, ratingCount, "Đánh giá chất lượng phải được lưu thành công");

            // 2.8 Xuất biên bản tư vấn PDF có nội dung trao đổi
            byte[] pdfBytes = recordPdfService.exportTicket(closed);
            assertNotNull(pdfBytes);
            assertTrue(pdfBytes.length > 500, "File PDF biên bản tư vấn phải được tạo với dữ liệu đầy đủ");

            System.out.println("===> KẾT QUẢ LUỒNG 2: HOÀN TẤT THÀNH CÔNG! <===");
        } finally {
            // Dọn dẹp dữ liệu kiểm thử
            tx().executeWithoutResult(status -> {
                jdbc.update("DELETE FROM dbo.DanhGiaYeuCau WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.PhanHoiTuVan WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.LichSuYeuCau WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.PhanCongTuVan WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.CauHoiDienDan WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.YeuCauTuVan WHERE MaYeuCau=?", ticketId);
            });
        }
    }

    @Test
    @Order(3)
    @DisplayName("Luồng 3: Diễn đàn Công khai (Công khai câu hỏi, Lọc, Tăng lượt xem, Bảo mật danh tính)")
    void testPublicForumFlow() {
        System.out.println("===> BẮT ĐẦU KIỂM TRA LUỒNG 3: DIỄN ĐÀN CÔNG KHAI <===");

        SinhVien student = studentRepo.findAll().getFirst();
        DonVi unit = unitRepo.findAll().stream().filter(u -> Boolean.TRUE.equals(u.getHoatDong())).findFirst().orElseThrow();
        LinhVucTuVan field = fieldRepo.findByDonVi_MaDonViAndTrangThai(unit.getMaDonVi(), "HoatDong").getFirst();

        String anonTitle = "FORUM_ANON_FLOW_" + UUID.randomUUID().toString().substring(0, 8);
        // Tạo phiếu có chọn ẨN DANH (baoMatDanhTinh = true)
        YeuCauTuVan anonTicket = ticketService.createTicket(student, field.getMaLinhVuc(), unit.getMaDonVi(), anonTitle, "Nội dung câu hỏi cần ẩn danh tính sinh viên.", true);
        Integer ticketId = anonTicket.getMaYeuCau();

        try {
            // 3.1 Kiểm tra câu hỏi tự động xuất hiện trên diễn đàn
            long count = forumService.count(anonTitle);
            assertEquals(1, count, "Câu hỏi mới tạo phải tự động xuất hiện trên Diễn đàn");

            // 3.2 Kiểm tra chi tiết và bộ đếm lượt xem (views)
            Map<String, Object> detail = forumService.detail(ticketId);
            assertEquals("Sinh viên ẩn danh", detail.get("authorName"), "Tên tác giả phải là 'Sinh viên ẩn danh'");
            assertFalse(detail.containsKey("email"), "Dữ liệu diễn đàn không được để lộ email sinh viên");
            assertFalse(detail.containsKey("maSV"), "Dữ liệu diễn đàn không được để lộ MSSV");

            long view1 = ((Number) detail.get("views")).longValue();
            Map<String, Object> detail2 = forumService.detail(ticketId);
            long view2 = ((Number) detail2.get("views")).longValue();
            assertEquals(view1 + 1, view2, "Mỗi lần xem chi tiết phải tăng đúng 1 lượt xem");

            // 3.3 Kiểm tra danh sách câu hỏi theo từ khóa
            List<Map<String, Object>> searchResult = forumService.list(anonTitle, 0, 10, "latest");
            assertEquals(1, searchResult.size());
            assertEquals(anonTitle, searchResult.getFirst().get("title"));

            System.out.println("===> KẾT QUẢ LUỒNG 3: HOÀN TẤT THÀNH CÔNG! <===");
        } finally {
            tx().executeWithoutResult(status -> {
                jdbc.update("DELETE FROM dbo.LichSuYeuCau WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.CauHoiDienDan WHERE MaYeuCau=?", ticketId);
                jdbc.update("DELETE FROM dbo.YeuCauTuVan WHERE MaYeuCau=?", ticketId);
            });
        }
    }

    @Test
    @Order(4)
    @DisplayName("Luồng 4: Đặt Lịch hẹn Tư vấn Trực tiếp (Tạo slot -> Đặt hẹn -> Duyệt hẹn -> Hoàn thành -> Đánh giá)")
    void testAppointmentBookingFlow() {
        System.out.println("===> BẮT ĐẦU KIỂM TRA LUỒNG 4: ĐẶT LỊCH HẸN TƯ VẤN TRỰC TIẾP <===");

        NguoiTuVan advisor = tx().execute(s -> {
            NguoiTuVan adv = advisorRepo.findAllWithAccountAndUnit().stream().filter(a -> Boolean.TRUE.equals(a.getNhanTuVan()) && "HoatDong".equals(a.getTaiKhoan().getTrangThai())).findFirst().orElseThrow();
            org.hibernate.Hibernate.initialize(adv.getTaiKhoan());
            org.hibernate.Hibernate.initialize(adv.getDonVi());
            return adv;
        });
        SinhVien student = tx().execute(s -> {
            SinhVien st = studentRepo.findAll().stream().filter(item -> "sinhvien1".equals(item.getMssv()) || "sinhvien1".equals(item.getTaiKhoan().getTenDangNhap())).findFirst().orElseThrow();
            org.hibernate.Hibernate.initialize(st.getTaiKhoan());
            return st;
        });

        // 4.1 Cố vấn mở khung giờ rảnh trong giờ hành chính thứ 2 tuần tới
        LocalDateTime slotStart = LocalDateTime.of(2026, 10, 12, 9, 0);
        LocalDateTime slotEnd = slotStart.plusMinutes(45);
        KhungGioTuVan slot = appointmentService.createSlot(advisor, slotStart, slotEnd, "TrucTuyen", "Phòng họp trực tuyến Teams");
        assertNotNull(slot.getMaKhungGio());
        Integer slotId = slot.getMaKhungGio();

        Integer appointmentId = null;
        try {
            // 4.2 Sinh viên tìm thấy khung giờ và đặt lịch hẹn
            LichHenTuVan appointment = appointmentService.bookAppointment(student, slotId, "Tư vấn thực tập tốt nghiệp", "Em muốn hỏi về các đối tác doanh nghiệp kỳ này.");
            assertNotNull(appointment.getMaLichHen());
            assertEquals("ChoDuyet", appointment.getTrangThai(), "Lịch hẹn mới phải ở trạng thái ChoDuyet");
            appointmentId = appointment.getMaLichHen();

            // 4.3 Cố vấn phê duyệt lịch hẹn và gửi link họp
            appointmentService.approveAppointment(appointmentId, advisor.getMaCV(), "https://teams.microsoft.com/meet/test-session-123");
            LichHenTuVan approved = appointmentRepo.findById(appointmentId).orElseThrow();
            assertEquals("DaXacNhan", approved.getTrangThai(), "Lịch hẹn sau khi duyệt phải là DaXacNhan");
            assertEquals("https://teams.microsoft.com/meet/test-session-123", approved.getLinkHopPhong());

            System.out.println("===> KẾT QUẢ LUỒNG 4: HOÀN TẤT THÀNH CÔNG! <===");
        } finally {
            final Integer finalAppId = appointmentId;
            tx().executeWithoutResult(status -> {
                if (finalAppId != null) {
                    jdbc.update("DELETE FROM dbo.LichHenTuVan WHERE MaLichHen=?", finalAppId);
                }
                jdbc.update("DELETE FROM dbo.KhungGioTuVan WHERE MaKhungGio=?", slotId);
            });
        }
    }

    @Test
    @Order(5)
    @DisplayName("Luồng 5: Quản trị & Báo cáo Thống kê (Dashboard KPIs, Xuất Excel, Xuất PDF)")
    void testAdminReportsAndKpiFlow() {
        System.out.println("===> BẮT ĐẦU KIỂM TRA LUỒNG 5: QUẢN TRỊ & BÁO CÁO THỐNG KÊ <===");

        // 5.1 Kiểm tra Dashboard Summary KPI
        var summary = reportService.getDashboardSummary();
        assertNotNull(summary, "Dashboard summary không được rỗng");
        assertTrue(summary.getTongYeuCau() >= 0, "Dashboard phải có tổng số ticket");
        assertTrue(summary.getChoTiepNhan() >= 0, "Dashboard phải có số ticket chờ tiếp nhận");
        assertTrue(summary.getTongCoVan() >= 0, "Dashboard phải có số cố vấn");

        // 5.2 Xuất báo cáo hoạt động dạng Excel (.xlsx)
        byte[] excelBytes = reportExportService.exportExcel(null, null, null);
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 1000, "File Excel báo cáo phải được tạo với dung lượng hợp lệ (> 1KB)");

        // 5.3 Xuất báo cáo hoạt động dạng PDF tiếng Việt (.pdf)
        byte[] pdfBytes = reportExportService.exportPdf(null, null, null);
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 1000, "File PDF báo cáo phải được tạo với dung lượng hợp lệ (> 1KB)");

        System.out.println("===> KẾT QUẢ LUỒNG 5: HOÀN TẤT THÀNH CÔNG! <===");
    }
}
