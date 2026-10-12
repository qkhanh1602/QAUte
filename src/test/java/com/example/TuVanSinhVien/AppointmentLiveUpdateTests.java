package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties="app.workflow.scheduling.enabled=false") @ActiveProfiles("dev")
class AppointmentLiveUpdateTests {
 @Autowired AppointmentService appointments;
 @Autowired OfficeAppointmentRequests officeRequests;
 @Autowired SinhVienRepository students;
 @Autowired NguoiTuVanRepository advisors;
 @Autowired KhungGioTuVanRepository slots;
 @Autowired LichHenTuVanRepository bookings;
 @Autowired SlaService calendar;
 @Autowired Clock clock;
 @Autowired PlatformTransactionManager transactions;
 @Autowired JdbcTemplate jdbc;
 @MockitoBean SimpMessagingTemplate messaging;
 @Test @org.springframework.transaction.annotation.Transactional
 void studentRequestsAnOfficeHourWithoutAdvisorPublishingSlotAndCompetingRequestIsBlocked() {
  var advisor=advisors.findAvailableAdvisors().getFirst();
  var student=students.findAll().getFirst();
  var now=LocalDateTime.now(clock);
  jdbc.update("UPDATE dbo.LichHenTuVan SET TrangThai='TuChoi',NgayDuyet=? WHERE MaSV=? AND TrangThai='ChoDuyet'",now,student.getMaSV());
  long before=slots.count();
  var time=officeRequests.getCalendar(advisor.getMaCV()).stream()
    .flatMap(day->((java.util.List<java.util.Map<String,Object>>)day.get("times")).stream())
    .filter(t->Boolean.TRUE.equals(t.get("available")))
    .filter(t->!bookings.existsOverlappingStudentAppointment(student.getMaSV(),(LocalDateTime)t.get("start"),(LocalDateTime)t.get("end")))
    .findFirst().orElseThrow();
  var start=(LocalDateTime)time.get("start");
  var booking=officeRequests.request(student,advisor.getMaCV(),start,"TrucTuyen","TEST_OFFICE_REQUEST","Nội dung chuẩn bị tư vấn.");
  assertEquals("ChoDuyet",booking.getTrangThai());
  assertEquals(start,booking.getKhungGio().getBatDau());
  assertEquals(start.plusHours(1),booking.getKhungGio().getKetThuc());
  assertNull(booking.getLinkHopPhong());
  assertEquals(before+1,slots.count());
  assertTrue(officeRequests.getCalendar(advisor.getMaCV()).stream()
    .flatMap(day->((java.util.List<java.util.Map<String,Object>>)day.get("times")).stream())
    .anyMatch(t->start.equals(t.get("start")) && Boolean.FALSE.equals(t.get("available"))));
  assertThrows(IllegalStateException.class,()->officeRequests.request(student,advisor.getMaCV(),start,"TrucTiep","Trùng lịch","Không được đặt trùng."));
 }
 @Test @org.springframework.transaction.annotation.Transactional
 void officeRequestRejectsWeekendAndDoesNotCreateSlot() {
  var advisor=advisors.findAvailableAdvisors().getFirst();
  var student=students.findAll().getFirst();
  var saturday=LocalDate.now(clock).with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.SATURDAY)).atTime(9,0);
  long before=slots.count();
  assertThrows(IllegalArgumentException.class,()->officeRequests.request(student,advisor.getMaCV(),saturday,"TrucTiep","Cuối tuần","Không hợp lệ."));
  assertEquals(before,slots.count());
 }
 @Test void changesBroadcastOnlyAfterSuccessfulCommitAndNeverIncludePrivateData() {
  var tx=new TransactionTemplate(transactions);
  var advisor=tx.execute(status->advisors.findAllWithAccountAndUnit().stream()
    .filter(a->Boolean.TRUE.equals(a.getNhanTuVan()) && "HoatDong".equals(a.getTaiKhoan().getTrangThai())).findFirst().orElseThrow());
  LocalDate day=LocalDate.now(clock).plusDays(1);
  while(!calendar.isOfficeInterval(day.atTime(14,0),day.atTime(14,45)))day=day.plusDays(1);
  final LocalDateTime start=day.atTime(14,0);
  clearInvocations(messaging);
  tx.execute(status->{appointments.createSlot(advisor,start,start.plusMinutes(45),"TrucTiep","TEST_ROLLBACK_LIVE");status.setRollbackOnly();return null;});
  verifyNoInteractions(messaging);
  Integer id=tx.execute(status->appointments.createSlot(advisor,start,start.plusMinutes(45),"TrucTiep","TEST_COMMIT_LIVE").getMaKhungGio());
  try { verify(messaging).convertAndSend("/queue/appointments",(Object) java.util.Map.of("type","changed")); }
  finally {jdbc.update("DELETE FROM dbo.KhungGioTuVan WHERE MaKhungGio=?",id);}
 }
}
