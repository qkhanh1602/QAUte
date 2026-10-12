package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.entity.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class WorkflowRulesTest {
    @Test void slaSkipsLunchWeekendHolidayAndPreservesSeconds() {
        SlaService sla=new SlaService("2026-10-05");
        assertEquals(LocalDateTime.of(2026,10,6,9,30,15),sla.calculateBusinessDeadline(LocalDateTime.of(2026,10,2,16,30,15),2));
        assertEquals(LocalDateTime.of(2026,10,2,13,30),sla.calculateBusinessDeadline(LocalDateTime.of(2026,10,2,11,30),1));
        assertThrows(IllegalArgumentException.class,()->sla.calculateBusinessDeadline(LocalDateTime.now(),-1));
    }
    @Test void smtpFailureDoesNotIssueUsableOtp() {
        var repo=mock(YeuCauXacMinhOTPRepository.class);var email=mock(EmailService.class);
        var audit=mock(SecurityAuditService.class);
        when(repo.findTopByEmailAndMucDichOrderByNgayTaoDesc(anyString(),anyString())).thenReturn(Optional.empty());
        var service=new OtpService(repo,email,new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder(),audit);
        assertFalse(service.generateAndSendOtp("student@example.com","DangKy","127.0.0.1","test").isSuccess());
        var captor=org.mockito.ArgumentCaptor.forClass(YeuCauXacMinhOTP.class);
        verify(repo,times(2)).save(captor.capture());
        assertNotNull(captor.getValue().getDaDungLuc());
    }
    @Test void selfServiceCannotChangeVerifiedName() {
        var repo=mock(TaiKhoanRepository.class);
        TaiKhoan account=TaiKhoan.builder().maTK(1).hoTen("Nguyễn Văn A").build();
        when(repo.findById(1)).thenReturn(Optional.of(account));
        var service=new ProfileService(repo,mock(AvatarStorageService.class),new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
        assertThrows(IllegalArgumentException.class,()->service.updateProfile(1,"Tên giả","0901234567"));
        verify(repo,never()).save(any());
    }
    @Test void schedulerUsesInjectedClockForDeadlines() {
        var tickets=mock(TicketService.class);var repo=mock(YeuCauTuVanRepository.class);
        var appointments=mock(AppointmentService.class);
        Clock clock=Clock.fixed(Instant.parse("2026-10-03T01:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
        var scheduler=new WorkflowScheduler(tickets,repo,mock(TaiKhoanRepository.class),mock(NguoiTuVanRepository.class),appointments,mock(NotificationService.class),mock(EmailService.class),new SlaService(),clock);
        scheduler.processTicketDeadlines();
        verify(tickets).autoCloseExpiredTickets(LocalDateTime.of(2026,10,3,8,0));
        verify(appointments).expirePendingAppointments(LocalDateTime.of(2026,10,3,8,0));
    }
    @Test void advisorCannotAlterBookedSlot() {
        var slots=mock(KhungGioTuVanRepository.class);var apps=mock(LichHenTuVanRepository.class);var accounts=mock(TaiKhoanRepository.class);
        Clock clock=Clock.fixed(Instant.parse("2026-10-03T01:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
        TaiKhoan account=TaiKhoan.builder().maTK(1).build();NguoiTuVan advisor=NguoiTuVan.builder().maCV(1).taiKhoan(account).build();
        var slot=KhungGioTuVan.builder().maKhungGio(1).nguoiTuVan(advisor).batDau(LocalDateTime.of(2026,10,4,8,0)).build();
        when(accounts.findLockedById(1)).thenReturn(Optional.of(account));when(slots.findLockedById(1)).thenReturn(Optional.of(slot));when(apps.isSlotOccupied(1)).thenReturn(true);
        var service=new AppointmentService(slots,apps,mock(DanhGiaBuoiTuVanRepository.class),mock(NotificationService.class),accounts,clock);
        assertThrows(IllegalStateException.class,()->service.updateSlot(1,advisor,null,null,null,null,true));
        verify(slots,never()).save(any());
    }
}
