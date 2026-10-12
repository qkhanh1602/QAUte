package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatAndFieldTests {
    final TaiKhoan student = TaiKhoan.builder().maTK(1).tenDangNhap("student").hoTen("Sinh viên")
            .vaiTro("SinhVien").trangThai("HoatDong").build();
    final DonVi unit = DonVi.builder().maDonVi(2).hoatDong(true).build();
    final PhienChat session = PhienChat.builder().maPhienChat(3).donVi(unit)
            .sinhVien(SinhVien.builder().maSV(4).taiKhoan(student).build()).trangThai("ChoTiepNhan").build();
    final TinNhanChatRepository messages = mock(TinNhanChatRepository.class);
    final PhienChatRepository sessions = mock(PhienChatRepository.class);
    final CauHoiFAQRepository faqs = mock(CauHoiFAQRepository.class);
    final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    ChatService chat() {
        when(sessions.findById(3)).thenReturn(Optional.of(session));
        when(messages.save(any())).thenAnswer(inv -> { TinNhanChat m=inv.getArgument(0); m.setMaTinNhan(UUID.randomUUID().getMostSignificantBits()); return m; });
        return new ChatService(sessions, messages, mock(DonViRepository.class), mock(NguoiTuVanRepository.class), faqs, events);
    }
    @Test void waitingWithoutAdvisorGetsSystemAcknowledgementAndRealtimeDelivery() {
        chat().sendMessage(3,student,"Xin hỗ trợ", "VanBan");
        var saved=org.mockito.ArgumentCaptor.forClass(TinNhanChat.class);
        verify(messages,times(2)).save(saved.capture());
        var reply=saved.getAllValues().get(1);
        assertNull(reply.getTaiKhoanGui());
        assertTrue(reply.getNoiDung().contains("Đã nhận tin nhắn"));
        assertTrue(reply.getNoiDung().length()<200);
        verify(events,times(2)).publishEvent(any(ChatDeliveryEvent.class));
    }
    @Test void acknowledgementIsNotRepeatedForLaterUnmatchedMessages() {
        when(messages.countByPhienChat_MaPhienChatAndTaiKhoanGui_MaTK(3,1)).thenReturn(2L);
        chat().sendMessage(3,student,"Thông tin bổ sung", "VanBan");
        verify(messages,times(1)).save(any());
    }
    @Test void faqReplyIsConciseAndDoesNotImpersonateAdvisor() {
        when(faqs.searchPublicFaq("học bổng")).thenReturn(List.of(CauHoiFAQ.builder().cauTraLoi("A".repeat(800)).build()));
        chat().sendMessage(3,student,"học bổng", "VanBan");
        var saved=org.mockito.ArgumentCaptor.forClass(TinNhanChat.class);
        verify(messages,times(2)).save(saved.capture());
        assertTrue(saved.getAllValues().get(1).getNoiDung().length()<600);
        assertNull(saved.getAllValues().get(1).getTaiKhoanGui());
    }
    @Test void advisorAlreadyJoinedDoesNotReceiveBotInterruption() {
        session.setTrangThai("DangHoatDong");
        chat().sendMessage(3,student,"Xin hỗ trợ", "VanBan");
        verify(messages,times(1)).save(any());
        verifyNoInteractions(faqs);
    }
    @Test void anotherStudentCannotTriggerReplies() {
        var stranger=TaiKhoan.builder().maTK(99).trangThai("HoatDong").build();
        assertThrows(SecurityException.class,()->chat().sendMessage(3,stranger,"Xin hỗ trợ", "VanBan"));
        verifyNoInteractions(messages,events);
    }
    final DonViRepository units=mock(DonViRepository.class);
    final LinhVucTuVanRepository fields=mock(LinhVucTuVanRepository.class);
    TicketService tickets() {
        var service=mock(TicketService.class,CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(service,"donViRepo",units);
        ReflectionTestUtils.setField(service,"linhVucRepo",fields);
        when(units.lockById(2)).thenReturn(Optional.of(unit));
        return service;
    }
    @Test void typedExistingNameReusesUnitFieldIgnoringCaseAndWhitespace() {
        when(fields.findByDonVi_MaDonVi(2)).thenReturn(List.of(LinhVucTuVan.builder().maLinhVuc(5).donVi(unit).tenLinhVuc("Học bổng").build()));
        assertEquals(5,tickets().resolveQuestionField(2,null,"  học   bổng  "));
        verify(fields,never()).save(any());
    }
    @Test void newFieldBelongsToSelectedUnit() {
        when(fields.save(any())).thenAnswer(inv->{ LinhVucTuVan f=inv.getArgument(0); assertSame(unit,f.getDonVi()); assertEquals("HoatDong",f.getTrangThai()); f.setMaLinhVuc(6); return f; });
        assertEquals(6,tickets().resolveQuestionField(2,null,"Thực tập doanh nghiệp"));
    }
    @Test void pausedUnitAndOversizedFieldCannotCreateCatalogEntries() {
        unit.setHoatDong(false);
        assertThrows(IllegalArgumentException.class,()->tickets().resolveQuestionField(2,null,"Học phí"));
        assertThrows(IllegalArgumentException.class,()->tickets().resolveQuestionField(2,null,"a".repeat(101)));
        verify(fields,never()).save(any());
    }
}
