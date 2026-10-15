package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

class SlaPolicyTests {
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-16T04:35:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @Test void schedulerDoesNotScanOrNotifyTheRemoved24HourWarning() {
        var repo = mock(YeuCauTuVanRepository.class);
        var notifications = mock(NotificationService.class);
        var scheduler = new WorkflowScheduler(mock(TicketService.class), repo, mock(TaiKhoanRepository.class),
                mock(NguoiTuVanRepository.class), mock(AppointmentService.class), notifications,
                mock(EmailService.class), new SlaService(), clock);
        scheduler.processTicketDeadlines();
        verify(repo).findOverdueSla(LocalDateTime.now(clock));
        verify(repo, never()).findByTrangThaiOrderByNgayTaoDesc(anyString());
        verifyNoInteractions(notifications);
    }

    @Test void overdue48HourAlertKeepsDeduplicationAndEmailQueue() {
        var repo = mock(YeuCauTuVanRepository.class);
        var notifications = mock(NotificationService.class);
        var outbox = mock(EmailOutboxService.class);
        var manager = TaiKhoan.builder().maTK(7).email("manager@example.test").build();
        var ticket = YeuCauTuVan.builder().maYeuCau(3).tieuDe("Câu hỏi quá hạn")
                .donVi(DonVi.builder().tenDonVi("Khoa CNTT").taiKhoanTruongDonVi(manager).build()).build();
        when(repo.findOverdueSla(any())).thenReturn(List.of(ticket));
        var scheduler = new WorkflowScheduler(mock(TicketService.class), repo, mock(TaiKhoanRepository.class),
                mock(NguoiTuVanRepository.class), mock(AppointmentService.class), notifications,
                mock(EmailService.class), new SlaService(), clock);
        ReflectionTestUtils.setField(scheduler, "outbox", outbox);
        scheduler.processTicketDeadlines();
        verify(notifications).sendNotificationOnce(eq(manager), eq("SLA_RED_3"), contains("48 giờ"), anyString(), eq("SLA"), eq("/admin/tickets"));
        verify(outbox).queue(eq("SLA_RED_3_7"), eq("manager@example.test"), contains("48 giờ"), anyString());
    }

    @Test void deadlineIs48ElapsedHoursAcrossWeekendAndOfficeBreaks() {
        var start = LocalDateTime.of(2026,10,16,11,35);
        assertEquals(LocalDateTime.of(2026,10,18,11,35), new SlaService().calculateDeadline(start,48));
    }
}
