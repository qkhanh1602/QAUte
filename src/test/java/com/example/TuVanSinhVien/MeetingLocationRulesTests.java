package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.entity.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MeetingLocationRulesTests {
 private final LichHenTuVanRepository repo=mock(LichHenTuVanRepository.class);
 private final NotificationService notifications=mock(NotificationService.class);
 private final LocalDateTime now=LocalDateTime.of(2026,10,9,8,0);
 private final AppointmentService service=new AppointmentService(mock(KhungGioTuVanRepository.class),repo,
   mock(DanhGiaBuoiTuVanRepository.class),notifications,mock(TaiKhoanRepository.class),
   Clock.fixed(now.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant(),ZoneId.of("Asia/Ho_Chi_Minh")));
 private LichHenTuVan fixture(String state) {
  var advisor=NguoiTuVan.builder().maCV(1).build();
  var student=SinhVien.builder().taiKhoan(TaiKhoan.builder().maTK(2).build()).build();
  var slot=KhungGioTuVan.builder().hinhThuc("TrucTuyen").batDau(now.plusHours(2)).ketThuc(now.plusHours(3)).build();
  var app=LichHenTuVan.builder().maLichHen(1).nguoiTuVan(advisor).sinhVien(student).khungGio(slot)
   .trangThai(state).chuDe("Tư vấn học tập").hanDuyet(now.plusHours(1)).build();
  when(repo.findLockedById(1)).thenReturn(Optional.of(app)); return app;
 }
 @Test void approvalRejectsTextPlaceholderBlankAndUnsafeUrlsWithoutChangingState() {
  var app=fixture("ChoDuyet");
  for(String bad:Arrays.asList(null,""," ","Địa điểm/link sẽ được cố vấn xác nhận khi duyệt","Phòng A101",
    "javascript:alert(1)","http://meet.google.com/abc","https://","https://user:password@meet.google.com/abc")) {
   assertThrows(IllegalArgumentException.class,()->service.approveAppointment(1,1,bad),String.valueOf(bad));
   assertEquals("ChoDuyet",app.getTrangThai()); assertNull(app.getLinkHopPhong());
  }
  verify(repo,never()).save(any()); verifyNoInteractions(notifications);
 }
 @Test void approvalAndLaterCorrectionRequireRealHttpsUrlAndNotifyStudent() {
  var app=fixture("ChoDuyet");
  service.approveAppointment(1,1,"https://meet.google.com/abc-defg-hij");
  assertEquals("DaXacNhan",app.getTrangThai());
  service.updateMeetingLocation(1,1,"https://example.zoom.us/j/123456789");
  assertEquals("https://example.zoom.us/j/123456789",app.getLinkHopPhong());
  verify(notifications,times(2)).sendNotification(any(),anyString(),anyString(),eq("LichHen"),eq("/student/appointments"));
 }
 @Test void correctionCannotBeMadeByAnotherAdvisorAfterCancellationOrAfterEnd() {
  var app=fixture("DaXacNhan");
  assertThrows(SecurityException.class,()->service.updateMeetingLocation(1,99,"https://meet.google.com/abc"));
  app.setTrangThai("Huy");
  assertThrows(IllegalStateException.class,()->service.updateMeetingLocation(1,1,"https://meet.google.com/abc"));
  app.setTrangThai("DaXacNhan"); app.getKhungGio().setKetThuc(now);
  assertThrows(IllegalStateException.class,()->service.updateMeetingLocation(1,1,"https://meet.google.com/abc"));
  verify(repo,never()).save(any()); verifyNoInteractions(notifications);
 }
 @Test void inPersonStillAcceptsSpecificRoomInsteadOfUrl() {
  var app=fixture("ChoDuyet");app.getKhungGio().setHinhThuc("TrucTiep");
  service.approveAppointment(1,1,"Phòng A101, tòa nhà A");
  assertEquals("DaXacNhan",app.getTrangThai());
 }
 @Test void studentCancellationChangesStateAndReleasesSlotWhenThreeHoursRemain() {
  var app=fixture("DaXacNhan");app.getSinhVien().setMaSV(2);
  app.getKhungGio().setBatDau(now.plusHours(3));
  assertEquals("",service.cancellationBlockReason(app));
  service.cancelAppointment(1,2,"Bị trùng lịch học");
  assertEquals("Huy",app.getTrangThai());assertEquals("Bị trùng lịch học",app.getLyDo());
  verify(repo).save(app);
 }
 @Test void cancellationWithinThreeHoursIsRejectedWithTheSameReasonShownInUi() {
  var app=fixture("DaXacNhan");app.getSinhVien().setMaSV(2);
  app.getKhungGio().setBatDau(now.plusHours(3).minusSeconds(1));
  String blocked=service.cancellationBlockReason(app);
  assertTrue(blocked.contains("3 giờ"));
  var error=assertThrows(IllegalStateException.class,()->service.cancelAppointment(1,2,"Trùng lịch"));
  assertEquals(blocked,error.getMessage());assertEquals("DaXacNhan",app.getTrangThai());
  verify(repo,never()).save(any());
 }
 @Test void inPersonCancellationKeepsTwelveHourBoundary() {
  var app=fixture("DaXacNhan"); app.getSinhVien().setMaSV(2);
  app.getKhungGio().setHinhThuc("TrucTiep"); app.getKhungGio().setBatDau(now.plusHours(12));
  assertEquals(now,service.cancellationDeadline(app));
  assertEquals("",service.cancellationBlockReason(app));
  service.cancelAppointment(1,2,"Trùng lịch học"); assertEquals("Huy",app.getTrangThai());
 }
 @Test void inPersonCancellationLessThanTwelveHoursIsRejectedEvenWithMoreThanThreeHoursLeft() {
  var app=fixture("DaXacNhan"); app.getSinhVien().setMaSV(2);
  app.getKhungGio().setHinhThuc("TrucTiep"); app.getKhungGio().setBatDau(now.plusHours(12).minusSeconds(1));
  assertTrue(service.cancellationBlockReason(app).contains("12 giờ"));
  assertThrows(IllegalStateException.class,()->service.cancelAppointment(1,2,"Trùng lịch học"));
  assertEquals("DaXacNhan",app.getTrangThai()); verify(repo,never()).save(any());
 }
}
