package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.controller.AdvisorController;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ConsultationFlowTests {
    final TaiKhoan account=TaiKhoan.builder().maTK(1).vaiTro("CoVan").build();
    final NguoiTuVan advisor=NguoiTuVan.builder().maCV(2).taiKhoan(account).donVi(DonVi.builder().maDonVi(3).build()).build();
    final NguoiTuVanRepository advisors=mock(NguoiTuVanRepository.class);
    final ChatService chat=mock(ChatService.class);
    AdvisorController controller() {
        when(advisors.findByTaiKhoan_MaTK(1)).thenReturn(Optional.of(advisor));
        return new AdvisorController(advisors,mock(TicketService.class),mock(AppointmentService.class),chat,
                mock(CatalogService.class),mock(NotificationService.class),mock(AttachmentService.class),mock(ConsultationRecordPdfService.class));
    }
    @Test void acceptingOutsideOfficeHoursRedirectsWithFriendlyErrorInsteadOf500() {
        var c=controller(); var flash=new RedirectAttributesModelMap();
        doThrow(new IllegalStateException("Ngoài giờ hành chính")).when(chat).advisorJoinSession(4,advisor);
        assertEquals("redirect:/advisor/chat",c.joinChat(4,new CustomUserDetails(account),flash));
        assertEquals("Ngoài giờ hành chính",flash.getFlashAttributes().get("errorMessage"));
    }
    @Test void successfulAcceptRedirectsToConversation() {
        assertEquals("redirect:/advisor/chat?sessionId=4",controller().joinChat(4,new CustomUserDetails(account),new RedirectAttributesModelMap()));
        verify(chat).advisorJoinSession(4,advisor);
    }
    @Test void viewingWaitingConversationNeverAcceptsItThroughGet() {
        var c=controller(); var model=new ExtendedModelMap();
        when(chat.getAdvisorSession(4,advisor)).thenReturn(PhienChat.builder().maPhienChat(4).build());
        assertEquals("advisor/chat",c.chatDashboard(new CustomUserDetails(account),4,model));
        verify(chat,never()).advisorJoinSession(anyInt(),any());
        assertNotNull(model.get("errorMessage")); assertNull(model.get("currentSession"));
    }
    @Test void closedQuestionsDoNotCountAsActiveButRemainAvailableInArchive() {
        var repo=mock(YeuCauTuVanRepository.class); var service=mock(TicketService.class,CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(service,"ticketRepo",repo);
        when(repo.findAdvisorTicketsByDeadline(2)).thenReturn(List.of(
            YeuCauTuVan.builder().trangThai("DangXuLy").build(),YeuCauTuVan.builder().trangThai("DaPhanHoi").build(),YeuCauTuVan.builder().trangThai("Dong").build()));
        assertEquals(2,service.getAdvisorAssignedTickets(2).size());assertEquals(1,service.getAdvisorClosedTickets(2).size());
    }
    @Test void everyFacultyHasItsOwnSuggestionsAndThreeSupportUnitsShareASet() {
        var common=ChatQuickGuide.forUnit("DAOTAO"); assertEquals(common,ChatQuickGuide.forUnit("CTSV"));assertEquals(common,ChatQuickGuide.forUnit("HOTRO"));
        var codes=List.of("CNTT","CKCTM","GTNL","CNHHTP","DDT","INTT","KHUD","KINHTE","NGOAINGU","TTDL","XAYDUNG","CTLUAT","VSPKT","DTTT");
        var sets=new HashSet<List<ChatQuickGuide.Suggestion>>();
        for(var code:codes) { var set=ChatQuickGuide.forUnit(code); assertEquals(4,set.size());assertNotEquals(common,set);sets.add(set); }
        assertEquals(codes.size(),sets.size());
    }
    @Test void oldSystemLabelsAreHiddenWithoutChangingHumanMessages() {
        var m=TinNhanChat.builder().noiDung("[🤖 Trả lời tự động]\nChào em").build();
        assertEquals("Chào em",m.getNoiDungHienThi());
        m.setTaiKhoanGui(account);assertEquals(m.getNoiDung(),m.getNoiDungHienThi());
    }
    @Test void greetingIncludesStudentNameAndStudentNumber() {
        var sessions=mock(PhienChatRepository.class);var messages=mock(TinNhanChatRepository.class);
        var student=TaiKhoan.builder().maTK(10).tenDangNhap("student").hoTen("Nguyễn An").trangThai("HoatDong").build();
        var session=PhienChat.builder().maPhienChat(4).donVi(DonVi.builder().maDonViCode("CNTT").build())
            .sinhVien(SinhVien.builder().taiKhoan(student).mssv("24110001").build()).trangThai("ChoTiepNhan").build();
        when(sessions.findById(4)).thenReturn(Optional.of(session));
        when(messages.save(any())).thenAnswer(inv->{TinNhanChat m=inv.getArgument(0);m.setMaTinNhan(5L);return m;});
        var service=new ChatService(sessions,messages,mock(DonViRepository.class),advisors,mock(CauHoiFAQRepository.class),mock(ApplicationEventPublisher.class));
        service.sendMessage(4,student,"Chào thầy", "VanBan");
        var captured=org.mockito.ArgumentCaptor.forClass(TinNhanChat.class);verify(messages,times(2)).save(captured.capture());
        assertTrue(captured.getAllValues().get(1).getNoiDung().contains("Chào em Nguyễn An - 24110001"));
        assertFalse(captured.getAllValues().get(1).getNoiDung().contains("Trả lời tự động"));
    }
    @Test void twoAdvisorsCannotTakeOwnershipOfTheSameConversation() {
        var sessions=mock(PhienChatRepository.class);var session=PhienChat.builder().maPhienChat(4).donVi(advisor.getDonVi()).nguoiTuVan(advisor).trangThai("DangHoatDong").build();
        when(sessions.findLockedById(4)).thenReturn(Optional.of(session));
        var service=new ChatService(sessions,mock(TinNhanChatRepository.class),mock(DonViRepository.class),advisors,mock(CauHoiFAQRepository.class),mock(ApplicationEventPublisher.class));
        ReflectionTestUtils.setField(service,"clock",Clock.fixed(Instant.parse("2026-10-13T03:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh")));
        var other=NguoiTuVan.builder().maCV(5).donVi(advisor.getDonVi()).build();
        assertThrows(SecurityException.class,()->service.advisorJoinSession(4,other));verify(sessions,never()).save(any());
    }
}
