package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.config.AdvisorRoleModelAdvice;
import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.controller.AdvisorController;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudentAdvisorRulesTests {

    private TaiKhoanRepository accountRepo;
    private SinhVienRepository studentRepo;
    private NguoiTuVanRepository advisorRepo;
    private DonViRepository unitRepo;
    private YeuCauTuVanRepository ticketRepo;
    private LichHenTuVanRepository appointmentRepo;
    private PasswordEncoder passwordEncoder;
    private UserAccountService userAccountService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        accountRepo = mock(TaiKhoanRepository.class);
        studentRepo = mock(SinhVienRepository.class);
        advisorRepo = mock(NguoiTuVanRepository.class);
        unitRepo = mock(DonViRepository.class);
        ticketRepo = mock(YeuCauTuVanRepository.class);
        appointmentRepo = mock(LichHenTuVanRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        fixedClock = Clock.fixed(Instant.parse("2026-10-29T10:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

        userAccountService = new UserAccountService(
                accountRepo, studentRepo, passwordEncoder, advisorRepo, unitRepo, ticketRepo, appointmentRepo
        );
    }

    @Test
    void testStudentPromotedToAdvisorAutomaticallyGetsNhanTuVanFalse() {
        TaiKhoan studentAcc = TaiKhoan.builder()
                .maTK(101)
                .tenDangNhap("sv_test")
                .hoTen("Sinh Vien Test")
                .email("sv_test@example.com")
                .vaiTro("SinhVien")
                .trangThai("HoatDong")
                .build();

        SinhVien sv = SinhVien.builder()
                .maSV(55)
                .mssv("24110252")
                .khoa("Cong nghe thong tin")
                .lop("241101")
                .taiKhoan(studentAcc)
                .build();

        DonVi itUnit = DonVi.builder()
                .maDonVi(10)
                .maDonViCode("CNTT")
                .tenDonVi("Khoa Cong nghe Thong tin")
                .hoatDong(true)
                .build();

        when(accountRepo.findLockedById(101)).thenReturn(Optional.of(studentAcc));
        when(accountRepo.findByEmail("sv_test@example.com")).thenReturn(Optional.of(studentAcc));
        when(studentRepo.findByTaiKhoan_MaTK(101)).thenReturn(Optional.of(sv));
        when(advisorRepo.findByTaiKhoan_MaTK(101)).thenReturn(Optional.empty());
        when(unitRepo.findById(10)).thenReturn(Optional.of(itUnit));
        when(advisorRepo.save(any(NguoiTuVan.class))).thenAnswer(inv -> inv.getArgument(0));

        // Admin (actorId=1) promotes student (id=101) to "CoVan" with unitId=10
        userAccountService.editAccount(101, 1, "Sinh Vien Test", "sv_test@example.com", "0901234567", "CoVan", 10);

        ArgumentCaptor<NguoiTuVan> advisorCaptor = ArgumentCaptor.forClass(NguoiTuVan.class);
        verify(advisorRepo).save(advisorCaptor.capture());
        NguoiTuVan createdAdvisor = advisorCaptor.getValue();

        assertEquals(studentAcc, createdAdvisor.getTaiKhoan());
        assertEquals(itUnit, createdAdvisor.getDonVi());
        // Verify nhanTuVan is FALSE: peer advisors cannot receive appointment bookings
        assertTrue(createdAdvisor.getNhanTuVan(), "Cố vấn sinh viên được kích hoạt nhận tư vấn trực tuyến");
        assertEquals("CoVan", studentAcc.getVaiTro());
        assertEquals("Cố vấn sinh viên", createdAdvisor.getChucVu());
    }

    @Test
    void testStudentAdvisorBlockedFromAppointmentAndSlotEndpoints() throws Exception {
        NguoiTuVanRepository advRepo = mock(NguoiTuVanRepository.class);
        AppointmentService appService = mock(AppointmentService.class);
        SinhVienRepository sRepo = mock(SinhVienRepository.class);

        TaiKhoan tk = TaiKhoan.builder().maTK(202).vaiTro("CoVan").hoTen("Peer Advisor").build();
        CustomUserDetails userDetails = new CustomUserDetails(tk);
        DonVi unit = DonVi.builder().maDonVi(10).tenDonVi("CNTT").hoatDong(true).build();
        NguoiTuVan advisor = NguoiTuVan.builder().maCV(33).taiKhoan(tk).donVi(unit).nhanTuVan(false).build();

        when(advRepo.findByTaiKhoan_MaTK(202)).thenReturn(Optional.of(advisor));
        when(sRepo.findByTaiKhoan_MaTK(202)).thenReturn(Optional.of(SinhVien.builder().maSV(99).taiKhoan(tk).build()));

        AdvisorController controller = new AdvisorController(
                advRepo, mock(TicketService.class), appService, mock(ChatService.class),
                mock(CatalogService.class), mock(NotificationService.class), mock(AttachmentService.class),
                mock(ConsultationRecordPdfService.class)
        );

        // Inject sRepo via reflection
        java.lang.reflect.Field f = AdvisorController.class.getDeclaredField("sinhVienRepo");
        f.setAccessible(true);
        f.set(controller, sRepo);

        // 1. Calling /advisor/appointments should redirect to /advisor/dashboard
        ExtendedModelMap model = new ExtendedModelMap();
        String result = controller.appointments(userDetails, model);
        assertEquals("redirect:/advisor/dashboard", result);
        verifyNoInteractions(appService);

        // 2. Calling /advisor/slots should redirect to /advisor/dashboard
        String slotResult = controller.slots(userDetails, model);
        assertEquals("redirect:/advisor/dashboard", slotResult);

        // 3. Calling approveAppointment should redirect to /advisor/dashboard with error
        RedirectAttributesModelMap flash = new RedirectAttributesModelMap();
        String approveResult = controller.approveAppointment(1, userDetails, "https://meet.google.com/abc-xyz", flash);
        assertEquals("redirect:/advisor/dashboard", approveResult);
        assertTrue(flash.getFlashAttributes().containsKey("errorMessage"));

        // 4. Calling dashboard should not load appointments and have isStudentAdvisor = true
        ExtendedModelMap dashModel = new ExtendedModelMap();
        controller.dashboard(userDetails, dashModel);
        assertEquals(Boolean.TRUE, dashModel.get("isStudentAdvisor"));
        assertEquals(List.of(), dashModel.get("appointments"));
    }

    @Test
    void testAdvisorRoleModelAdviceIdentifiesPeerAdvisors() {
        SinhVienRepository sRepo = mock(SinhVienRepository.class);
        AdvisorRoleModelAdvice advice = new AdvisorRoleModelAdvice(sRepo);

        TaiKhoan peerTk = TaiKhoan.builder().maTK(301).vaiTro("CoVan").build();
        when(sRepo.findByTaiKhoan_MaTK(301)).thenReturn(Optional.of(SinhVien.builder().maSV(1).build()));

        TaiKhoan facultyTk = TaiKhoan.builder().maTK(302).vaiTro("CoVan").build();
        when(sRepo.findByTaiKhoan_MaTK(302)).thenReturn(Optional.empty());

        TaiKhoan studentTk = TaiKhoan.builder().maTK(303).vaiTro("SinhVien").build();

        assertTrue(advice.isStudentAdvisor(new CustomUserDetails(peerTk)), "Sinh viên được chuyển sang cố vấn phải là isStudentAdvisor=true");
        assertFalse(advice.isStudentAdvisor(new CustomUserDetails(facultyTk)), "Cố vấn giảng viên phải là isStudentAdvisor=false");
        assertFalse(advice.isStudentAdvisor(new CustomUserDetails(studentTk)), "Sinh viên bình thường phải là isStudentAdvisor=false");
        assertFalse(advice.isStudentAdvisor(null), "Null user phải là false");
    }

    @Test
    void testAppointmentServiceRejectsAdvisorWithNhanTuVanFalse() {
        LichHenTuVanRepository appRepo = mock(LichHenTuVanRepository.class);
        TaiKhoanRepository tkRepo = mock(TaiKhoanRepository.class);
        NotificationService notiService = mock(NotificationService.class);
        KhungGioTuVanRepository slotRepo = mock(KhungGioTuVanRepository.class);

        AppointmentService service = new AppointmentService(
                slotRepo, appRepo, mock(DanhGiaBuoiTuVanRepository.class), notiService, tkRepo, fixedClock
        );

        TaiKhoan advisorTk = TaiKhoan.builder().maTK(401).vaiTro("CoVan").trangThai("HoatDong").build();
        NguoiTuVan advisor = NguoiTuVan.builder().maCV(50).taiKhoan(advisorTk).nhanTuVan(false).build();
        KhungGioTuVan slot = KhungGioTuVan.builder().maKhungGio(1).nguoiTuVan(advisor).hinhThuc("TrucTuyen")
                .batDau(LocalDateTime.now(fixedClock).plusDays(1))
                .ketThuc(LocalDateTime.now(fixedClock).plusDays(1).plusHours(1)).build();
        LichHenTuVan app = LichHenTuVan.builder().maLichHen(10).nguoiTuVan(advisor).khungGio(slot).trangThai("ChoDuyet")
                .hanDuyet(LocalDateTime.now(fixedClock).plusHours(5)).build();

        when(appRepo.findLockedById(10)).thenReturn(Optional.of(app));

        // When approveAppointment is called, requireAppointmentAdvisor checks nhanTuVan
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            service.approveAppointment(10, 50, "https://meet.google.com/abc-xyz");
        });
        assertTrue(ex.getMessage().contains("không phụ trách chức năng hẹn lịch tư vấn"));
    }
}
