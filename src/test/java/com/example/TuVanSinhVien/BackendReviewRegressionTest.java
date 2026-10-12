package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.config.*;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.simp.*;
import org.springframework.messaging.support.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackendReviewRegressionTest {
    final Clock clock=Clock.fixed(Instant.parse("2026-10-05T02:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
    final TaiKhoanRepository accounts=mock(TaiKhoanRepository.class);
    TaiKhoan account(int id, String role) {
        return TaiKhoan.builder().maTK(id).tenDangNhap("probe"+id).hoTen("Probe")
                .vaiTro(role).trangThai("HoatDong").batBuocDoiMatKhau(false).tokenVersion(0).build();
    }
    org.springframework.messaging.Message<byte[]> frame(StompCommand command,String session, String destination,TaiKhoan user) {
        var headers=StompHeaderAccessor.create(command); headers.setSessionId(session); headers.setDestination(destination);
        if(user!=null) {var principal=new CustomUserDetails(user); headers.setUser(new UsernamePasswordAuthenticationToken(principal,null,principal.getAuthorities()));}
        return MessageBuilder.createMessage(new byte[0],headers.getMessageHeaders());
    }

    @Test void brokerRejectsAnonymousAndPhysicalSubscriptionsAndDirectSends() {
        var guard=new WebSocketAuthorization(accounts);var channel=mock(org.springframework.messaging.MessageChannel.class);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.CONNECT,"anon",null,null),channel));
        var user=account(1,"SinhVien");when(accounts.findById(1)).thenReturn(Optional.of(user));
        guard.preSend(frame(StompCommand.CONNECT,"s1",null,user),channel);
        assertNotNull(guard.preSend(frame(StompCommand.SUBSCRIBE,"s1","/queue/appointments",user),channel));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.SEND,"s1","/queue/appointments",user),channel));
        assertNotNull(guard.preSend(frame(StompCommand.SUBSCRIBE,"s1","/user/queue/chat",user),channel));
        for(var destination:List.of("/queue/chat-users2","/user/probe2/queue/chat","/queue/private"))
            assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.SUBSCRIBE,"s1",destination,user),channel));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.SEND,"s1","/queue/presence",user),channel));
        assertNotNull(guard.preSend(frame(StompCommand.SEND,"s1","/app/chat.send",user),channel));
    }

    @Test void revokedSocketCannotSendOrReceive() {
        var guard=new WebSocketAuthorization(accounts);var channel=mock(org.springframework.messaging.MessageChannel.class);
        var user=account(1,"SinhVien");when(accounts.findById(1)).thenReturn(Optional.of(user));
        guard.preSend(frame(StompCommand.CONNECT,"s1",null,user),channel);
        assertNotNull(guard.preSend(frame(StompCommand.SUBSCRIBE,"s1","/queue/appointments",user),channel));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.SEND,"s1","/queue/appointments",user),channel));
        var headers=SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);headers.setSessionId("s1");
        var outgoing=MessageBuilder.createMessage(new byte[0],headers.getMessageHeaders());
        assertNotNull(guard.outbound().preSend(outgoing,channel));
        user.setTokenVersion(1);
        assertThrows(org.springframework.security.access.AccessDeniedException.class,()->guard.preSend(frame(StompCommand.SEND,"s1","/app/chat.send",user),channel));
        assertNull(guard.outbound().preSend(outgoing,channel));
        user.setTokenVersion(0);user.setTrangThai("Khoa");
        assertNull(guard.outbound().preSend(outgoing,channel));
    }

    @Test void elapsedSlaCrossesWeekendAndHolidayButSlotsCannot() {
        var sla=new SlaService("2026-10-05");var friday=LocalDateTime.of(2026,10,2,16,30);
        assertEquals(LocalDateTime.of(2026,10,4,16,30),sla.calculateDeadline(friday,48));
        assertThrows(IllegalArgumentException.class,()->sla.requireOfficeInterval(LocalDateTime.of(2026,10,4,23,0),LocalDateTime.of(2026,10,5,0,0)));
        assertThrows(IllegalArgumentException.class,()->sla.requireOfficeInterval(LocalDateTime.of(2026,10,5,9,0),LocalDateTime.of(2026,10,5,10,0)));
        assertThrows(IllegalArgumentException.class,()->sla.requireOfficeInterval(LocalDateTime.of(2026,10,6,11,30),LocalDateTime.of(2026,10,6,12,30)));
        assertDoesNotThrow(()->sla.requireOfficeInterval(LocalDateTime.of(2026,10,6,11,0),LocalDateTime.of(2026,10,6,12,0)));
    }

    @Test void adminCannotBeLockedOrCreatedAgain() {
        var user=account(1,"Admin");when(accounts.findLockedById(1)).thenReturn(Optional.of(user));
        var service=new UserAccountService(accounts,mock(SinhVienRepository.class),mock(org.springframework.security.crypto.password.PasswordEncoder.class),mock(NguoiTuVanRepository.class),mock(DonViRepository.class),mock(YeuCauTuVanRepository.class),mock(LichHenTuVanRepository.class));
        assertThrows(IllegalArgumentException.class,()->service.toggleStatus(1));
        assertThrows(IllegalArgumentException.class,()->service.createAdmin("new", "Password1", "Admin", "a@example.test",null));
        verify(accounts,never()).save(any());assertEquals("HoatDong",user.getTrangThai());
    }

    @Test void bookingChecksAdvisorAndCapsApprovalAtStart() {
        var slots=mock(KhungGioTuVanRepository.class);var apps=mock(LichHenTuVanRepository.class);
        var advisorAccount=account(2,"CoVan");var studentAccount=account(1,"SinhVien");
        var advisor=NguoiTuVan.builder().maCV(2).taiKhoan(advisorAccount).nhanTuVan(true).donVi(DonVi.builder().maDonVi(1).hoatDong(true).build()).build();
        var student=SinhVien.builder().maSV(1).taiKhoan(studentAccount).build();
        var start=LocalDateTime.now(clock).plusMinutes(30);
        var slot=KhungGioTuVan.builder().maKhungGio(3).nguoiTuVan(advisor).trangThai("Mo").batDau(start).ketThuc(start.plusHours(1)).build();
        when(slots.findById(3)).thenReturn(Optional.of(slot));when(slots.findLockedById(3)).thenReturn(Optional.of(slot));
        when(accounts.findLockedById(1)).thenReturn(Optional.of(studentAccount));when(accounts.findLockedById(2)).thenReturn(Optional.of(advisorAccount));
        when(apps.save(any())).thenAnswer(i->i.getArgument(0));
        var service=new AppointmentService(slots,apps,mock(DanhGiaBuoiTuVanRepository.class),mock(NotificationService.class),accounts,clock);
        advisorAccount.setTrangThai("Khoa");assertThrows(IllegalStateException.class,()->service.bookAppointment(student,3,"Subject","Details"));
        advisorAccount.setTrangThai("HoatDong");advisor.setNhanTuVan(false);assertThrows(IllegalStateException.class,()->service.bookAppointment(student,3,"Subject","Details"));
        advisor.setNhanTuVan(true);advisor.getDonVi().setHoatDong(false);assertThrows(IllegalStateException.class,()->service.bookAppointment(student,3,"Subject","Details"));
        advisor.getDonVi().setHoatDong(true);assertEquals(start,service.bookAppointment(student,3,"Subject","Details").getHanDuyet());
    }

    @Test void elapsedPendingAppointmentIsExpiredEvenBeforeOldDeadline() {
        var slots=mock(KhungGioTuVanRepository.class);var apps=mock(LichHenTuVanRepository.class);
        var now=LocalDateTime.now(clock);var student=SinhVien.builder().taiKhoan(account(1,"SinhVien")).build();
        var app=LichHenTuVan.builder().maLichHen(9).trangThai("ChoDuyet").hanDuyet(now.plusHours(23)).sinhVien(student)
                .khungGio(KhungGioTuVan.builder().batDau(now.minusMinutes(30)).build()).build();
        when(apps.findExpiredPending(now)).thenReturn(List.of(app));when(apps.findLockedById(9)).thenReturn(Optional.of(app));
        var service=new AppointmentService(slots,apps,mock(DanhGiaBuoiTuVanRepository.class),mock(NotificationService.class),accounts,clock);
        assertEquals(1,service.expirePendingAppointments(now));assertEquals("TuChoi",app.getTrangThai());
    }

    @Test void invalidAttachmentPreventsAnyTicketMutation() {
        var tickets=mock(TicketService.class);var attachmentRepo=mock(TepDinhKemRepository.class);
        var attachments=new AttachmentService(mock(com.cloudinary.Cloudinary.class),new FileValidationService(),attachmentRepo);
        var service=new SubmissionService(tickets,mock(ChatService.class),attachments);
        var file=new org.springframework.mock.web.MockMultipartFile("attachment","bad.pdf","application/pdf","Not a PDF".getBytes());
        assertThrows(IllegalArgumentException.class,()->service.createTicket(null,1,1,"Title","Content",false,file));
        verifyNoInteractions(tickets,attachmentRepo);
    }

    @Test void failedSlaEmailIsRetriedAndThenMarkedSuccessful() {
        var jdbc=mock(org.springframework.jdbc.core.JdbcTemplate.class);var email=mock(EmailService.class);
        var manager=mock(org.springframework.transaction.PlatformTransactionManager.class);
        when(manager.getTransaction(any())).thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());
        var first=Map.<String,Object>of("MaSuKien",1L,"SoLanThu",0,"Payload","{\"email\":\"test@example.test\",\"subject\":\"Test\",\"html\":\"Body\"}");
        var second=new HashMap<>(first);second.put("SoLanThu",1);
        when(jdbc.queryForList(anyString(),any(Object[].class))).thenReturn(List.of(first),List.of(),List.of(second),List.of());
        when(email.sendNotificationEmail(anyString(),anyString(),anyString())).thenReturn(false,true);
        var service=new EmailOutboxService(jdbc,email,clock,manager);
        service.processDueEmails();service.processDueEmails();
        verify(jdbc).update(startsWith("UPDATE"),eq("ChoXuLy"),eq(1),any(LocalDateTime.class),eq("Dịch vụ email chưa gửi thành công"),eq(1L),eq(1));
        verify(jdbc).update(startsWith("UPDATE"),eq("ThanhCong"),eq(2),any(LocalDateTime.class),isNull(),eq(1L),eq(2));
        verify(email,times(2)).sendNotificationEmail(anyString(),anyString(),anyString());
    }

    @Test void workflowTimestampDoesNotFollowServerUtcTimezone() {
        var original=java.util.TimeZone.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            var repo=mock(ThongBaoRepository.class);var service=new NotificationService(repo);
            org.springframework.test.util.ReflectionTestUtils.setField(service,"clock",clock);
            service.sendNotification(account(1,"SinhVien"),"Title","Body","Test","/");
            var capture=org.mockito.ArgumentCaptor.forClass(ThongBao.class);verify(repo).save(capture.capture());
            assertEquals(LocalDateTime.of(2026,10,5,9,0),capture.getValue().getNgayTao());
        } finally {java.util.TimeZone.setDefault(original);}
    }

    @Test void outsideOfficeHoursStudentsMayQueueButAdvisorCannotReplyOrJoin() {
        var sessions=mock(PhienChatRepository.class);var messages=mock(TinNhanChatRepository.class);
        var events=mock(org.springframework.context.ApplicationEventPublisher.class);
        var service=new ChatService(sessions,messages,mock(DonViRepository.class),mock(NguoiTuVanRepository.class),mock(CauHoiFAQRepository.class),events);
        var sunday=Clock.fixed(Instant.parse("2026-10-04T02:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh"));
        org.springframework.test.util.ReflectionTestUtils.setField(service,"clock",sunday);
        org.springframework.test.util.ReflectionTestUtils.setField(service,"accounts",accounts);
        var advisor=account(2,"CoVan");when(accounts.findById(2)).thenReturn(Optional.of(advisor));
        assertThrows(IllegalStateException.class,()->service.sendMessage(1,advisor,"Reply","VanBan"));
        assertThrows(IllegalStateException.class,()->service.advisorJoinSession(1,NguoiTuVan.builder().taiKhoan(advisor).build()));
        var student=account(1,"SinhVien");when(accounts.findById(1)).thenReturn(Optional.of(student));
        var session=PhienChat.builder().maPhienChat(1).donVi(DonVi.builder().maDonVi(3).build()).sinhVien(SinhVien.builder().taiKhoan(student).build()).trangThai("ChoTiepNhan").build();
        when(sessions.findById(1)).thenReturn(Optional.of(session));
        when(messages.save(any())).thenAnswer(i->{var message=(TinNhanChat)i.getArgument(0);message.setMaTinNhan(1L);return message;});
        assertNotNull(service.sendMessage(1,student,"Câu hỏi chờ phản hồi","VanBan"));
        verify(events,times(2)).publishEvent(any(ChatDeliveryEvent.class));
    }

    @Test void dashboardUsesSame24HourCutoffWithVietnamClock() {
        var tickets=mock(YeuCauTuVanRepository.class);
        var reports=new ReportService(tickets,mock(LichHenTuVanRepository.class),accounts,mock(DonViRepository.class));
        org.springframework.test.util.ReflectionTestUtils.setField(reports,"clock",clock);
        reports.getDashboardSummary();
        verify(tickets).findOverdueAcceptance(LocalDateTime.of(2026,10,4,9,0));
    }
}
