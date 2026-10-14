package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.web.JspFunctions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockMultipartFile;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OfficeHoursAndDisplayTests {
    final SlaService hours = new SlaService();
    final LocalDate monday = LocalDate.of(2026,10,12);
    final Clock clock = Clock.fixed(monday.atTime(7,0).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),ZoneId.of("Asia/Ho_Chi_Minh"));
    final NguoiTuVanRepository advisors = mock(NguoiTuVanRepository.class);
    final TaiKhoanRepository accounts = mock(TaiKhoanRepository.class);
    final KhungGioTuVanRepository slots = mock(KhungGioTuVanRepository.class);
    final AppointmentService appointments = mock(AppointmentService.class);
    final TaiKhoan advisorAccount = TaiKhoan.builder().maTK(2).vaiTro("CoVan").trangThai("HoatDong").build();
    final NguoiTuVan advisor = NguoiTuVan.builder().maCV(2).taiKhoan(advisorAccount).nhanTuVan(true)
            .donVi(DonVi.builder().maDonVi(3).hoatDong(true).build()).build();
    OfficeAppointmentRequests proposals() {
        when(advisors.findById(2)).thenReturn(Optional.of(advisor));
        when(slots.findReservedAdvisorSlotsInRange(eq(2),any(),any(),any())).thenReturn(List.of());
        return new OfficeAppointmentRequests(advisors,accounts,slots,appointments,hours,clock);
    }

    @ParameterizedTest @ValueSource(strings={"08:00","11:29:59.999999999","13:30","16:29:59.999999999"})
    void acceptsTimesInsideNewWindows(String time) { assertTrue(hours.isOfficeTime(monday.atTime(LocalTime.parse(time)))); }
    @ParameterizedTest @ValueSource(strings={"07:59:59","11:30","12:00","13:00","13:29:59.999999999","16:30","17:00"})
    void rejectsTimesOutsideNewWindows(String time) { assertFalse(hours.isOfficeTime(monday.atTime(LocalTime.parse(time)))); }

    @Test void intervalsMustStayEntirelyInOneWindow() {
        assertTrue(hours.isOfficeInterval(monday.atTime(10,30),monday.atTime(11,30)));
        assertTrue(hours.isOfficeInterval(monday.atTime(15,30),monday.atTime(16,30)));
        assertFalse(hours.isOfficeInterval(monday.atTime(11,0),monday.atTime(12,0)));
        assertFalse(hours.isOfficeInterval(monday.atTime(13,0),monday.atTime(14,0)));
        assertFalse(hours.isOfficeInterval(monday.atTime(16,0),monday.atTime(17,0)));
        assertFalse(hours.isOfficeInterval(monday.atTime(11,0),monday.atTime(14,0)));
        assertFalse(hours.isOfficeInterval(monday.atTime(10,30),monday.atTime(11,30).plusNanos(1)));
        assertFalse(hours.isOfficeTime(monday.plusDays(5).atTime(8,0)));
        assertFalse(new SlaService("2026-10-12").isOfficeTime(monday.atTime(8,0)));
    }
    @Test void businessDeadlineSkipsLunchWhileSlaStillUsesElapsedHours() {
        assertEquals(monday.atTime(14,0),hours.calculateBusinessDeadline(monday.atTime(11,0),1));
        assertEquals(monday.plusDays(1).atTime(8,30),hours.calculateBusinessDeadline(monday.atTime(16,0),1));
        assertEquals(monday.plusDays(2).atTime(16,0),hours.calculateDeadline(monday.atTime(16,0),48));
    }
    @Test void proposedCalendarIncludesHalfHourStartsAndNoInvalidIntervals() {
        var calendar=proposals().getCalendar(2);
        @SuppressWarnings("unchecked") var first=(List<Map<String,Object>>)calendar.getFirst().get("times");
        var starts=first.stream().map(t->((LocalDateTime)t.get("start")).toLocalTime()).toList();
        assertEquals(List.of(LocalTime.of(8,0),LocalTime.of(8,30),LocalTime.of(9,0),LocalTime.of(9,30),
                LocalTime.of(10,0),LocalTime.of(10,30),LocalTime.of(13,30),LocalTime.of(14,0),LocalTime.of(14,30),LocalTime.of(15,0),LocalTime.of(15,30)),starts);
        for(var day:calendar) {
            @SuppressWarnings("unchecked") var times=(List<Map<String,Object>>)day.get("times");
            for(var time:times) assertTrue(hours.isOfficeInterval((LocalDateTime)time.get("start"),(LocalDateTime)time.get("end")));
        }
    }
    @Test void afternoonProposalStartsAt1330AndBooksOneHour() {
        var service=proposals();var student=SinhVien.builder().taiKhoan(TaiKhoan.builder().maTK(1).build()).build();
        when(accounts.findLockedById(1)).thenReturn(Optional.of(student.getTaiKhoan()));
        when(accounts.findLockedById(2)).thenReturn(Optional.of(advisorAccount));
        when(slots.save(any())).thenAnswer(call -> { KhungGioTuVan saved=call.getArgument(0); saved.setMaKhungGio(5); return saved; });
        service.request(student,2,monday.atTime(13,30),"TrucTiep","Chủ đề","Nội dung");
        verify(slots).save(argThat(s -> s.getBatDau().equals(monday.atTime(13,30)) && s.getKetThuc().equals(monday.atTime(14,30)) && "TrucTiep".equals(s.getHinhThuc())));
        verify(appointments,never()).createSlot(any(),any(),any(),any(),any());
        verify(appointments).bookAppointment(student,5,"Chủ đề","Nội dung");
    }
    @ParameterizedTest @ValueSource(strings={"11:00","13:00","16:00","14:15"})
    void forgedProposalCannotBypassHoursOrHalfHourGrid(String time) {
        var service=new OfficeAppointmentRequests(advisors,accounts,slots,appointments,hours,clock);
        assertThrows(IllegalArgumentException.class,()->service.request(null,2,monday.atTime(LocalTime.parse(time)),"TrucTiep","A","B"));
        verifyNoInteractions(advisors,accounts,slots,appointments);
    }
    @ParameterizedTest @ValueSource(strings={"11:30","13:00","16:30"})
    void advisorCannotJoinOrSendChatOutsideNewHours(String time) {
        var sessions=mock(PhienChatRepository.class);
        var chat=new ChatService(sessions,mock(TinNhanChatRepository.class),mock(DonViRepository.class),advisors,mock(CauHoiFAQRepository.class),mock(ApplicationEventPublisher.class));
        ReflectionTestUtils.setField(chat,"clock",Clock.fixed(monday.atTime(LocalTime.parse(time)).atZone(clock.getZone()).toInstant(),clock.getZone()));
        assertThrows(IllegalStateException.class,()->chat.advisorJoinSession(1,advisor));
        assertThrows(IllegalStateException.class,()->chat.sendMessage(1,advisorAccount,"Xin chào","VanBan"));
        verifyNoInteractions(sessions);
    }
    @Test void pendingOldAppointmentOutsideHoursCannotBeApproved() {
        var repo=mock(LichHenTuVanRepository.class);
        var service=new AppointmentService(slots,repo,mock(DanhGiaBuoiTuVanRepository.class),mock(NotificationService.class),accounts,clock);
        var app=LichHenTuVan.builder().nguoiTuVan(advisor).trangThai("ChoDuyet")
            .khungGio(KhungGioTuVan.builder().batDau(monday.atTime(13,0)).ketThuc(monday.atTime(14,0)).build()).build();
        when(repo.findLockedById(1)).thenReturn(Optional.of(app));
        assertThrows(IllegalArgumentException.class,()->service.approveAppointment(1,2,"Phòng A"));
        verify(repo,never()).save(any());
    }
    @Test void displayUsesVietnameseLabelsAndWeekdays() {
        assertEquals("Đang xử lý",JspFunctions.statusLabel("DangXuLy"));
        assertEquals("Đã từ chối",JspFunctions.statusLabel("TuChoi"));
        assertEquals("Phòng ban",JspFunctions.unitTypeLabel("PhongBan"));
        assertTrue(JspFunctions.formatTemporal(monday,"EEEE, dd/MM/yyyy").contains("Thứ Hai"));
        assertEquals("08:00–11:30 và 13:30–16:30",JspFunctions.officeHoursLabel());
    }
    @Test void attachmentMessageUsesTypeAllowedByDatabase() {
        var chat=mock(ChatService.class);var attachments=mock(AttachmentService.class);
        var service=new SubmissionService(mock(TicketService.class),chat,attachments);
        var file=new MockMultipartFile("file","test.pdf","application/pdf",new byte[]{1});
        var message=TinNhanChat.builder().build();
        when(chat.sendMessage(eq(1),eq(advisorAccount),anyString(),eq("TepTin"))).thenReturn(message);
        service.sendMessage(1,advisorAccount,"Tài liệu",file);
        verify(attachments).attachToMessage(file,advisorAccount,message);
    }
    @Test void relatedArticlesAreFilteredByCurrentArticleField() {
        var content=mock(ContentService.class); var related=List.of(BaiVietHuongDan.builder().maBaiViet(2).build());
        when(content.getArticleAndIncrementView(1)).thenReturn(BaiVietHuongDan.builder().maBaiViet(1)
                .linhVuc(LinhVucTuVan.builder().maLinhVuc(7).build()).build());
        when(content.getPublicArticles(7,null)).thenReturn(related);
        var controller=new com.example.TuVanSinhVien.controller.HomeController(content,mock(CatalogService.class),mock(ReportService.class),mock(AttachmentService.class));
        var model=new org.springframework.ui.ExtendedModelMap();
        assertEquals("public/article-detail",controller.articleDetail(1,model));
        assertEquals(related,model.get("recentArticles"));
        verify(content,never()).getPublicArticles(null,null);
    }
}
