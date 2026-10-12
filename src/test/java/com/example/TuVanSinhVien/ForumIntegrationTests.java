package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URI;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
 properties={"app.workflow.scheduling.enabled=false"})
class ForumIntegrationTests {
 @Autowired ForumService forum;
 @Autowired TicketService tickets;
 @Autowired SinhVienRepository students;
 @Autowired LinhVucTuVanRepository fields;
 @Autowired JdbcTemplate jdbc;
 @Autowired NguoiTuVanRepository advisors;
 @Autowired YeuCauTuVanRepository ticketRepository;
 @LocalServerPort int port;
 @Autowired JwtService jwt;
 @Autowired TaiKhoanRepository accounts;
 @Test void onlyStudentsCanOpenForumQuestionForm() throws Exception {
  var client=HttpClient.newHttpClient();
  var uri=URI.create("http://localhost:"+port+"/forum/ask");
  var guest=client.send(HttpRequest.newBuilder(uri).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(302,guest.statusCode());
  assertTrue(guest.headers().firstValue("location").orElse("").contains("/login"));
  var student=accounts.findById(students.findAll().getFirst().getTaiKhoan().getMaTK()).orElseThrow();
  var form=client.send(HttpRequest.newBuilder(uri).header("Authorization","Bearer "+jwt.createAccessToken(student)).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(200,form.statusCode());
  assertTrue(form.body().contains("/forum/ask"));
  assertTrue(form.body().contains("Đặt câu hỏi trên diễn đàn"));
  var advisor=accounts.findById(advisors.findAll().getFirst().getTaiKhoan().getMaTK()).orElseThrow();
  var forbidden=client.send(HttpRequest.newBuilder(uri).header("Authorization","Bearer "+jwt.createAccessToken(advisor)).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(403,forbidden.statusCode());
 }


 @Test @Transactional void advisorPrioritizesUnansweredTicketsByEarliestSlaBeforeCompletedTickets() {
  var advisor=advisors.findAll().getFirst();
  var student=students.findAll().getFirst();
  var field=fields.findByDonVi_MaDonViAndTrangThai(advisor.getDonVi().getMaDonVi(),"HoatDong").getFirst();
  var now=java.time.LocalDateTime.of(2026,10,8,12,0);
  var ids=new java.util.ArrayList<Integer>();
  for(int i=0;i<3;i++) {
   var created=now.minusDays(i==2?10:(i==0?1:2));
   var ticket=com.example.TuVanSinhVien.entity.YeuCauTuVan.builder()
     .sinhVien(student).donVi(advisor.getDonVi()).linhVuc(field).coVanPhuTrach(advisor)
     .tieuDe("TEST_SLA_PRIORITY_"+java.util.UUID.randomUUID())
     .noiDung("Dữ liệu tạm kiểm tra thứ tự ưu tiên phiếu theo SLA.")
     .trangThai(i==2?"Dong":"DangXuLy").ngayTao(created).ngayCapNhat(created)
     .ngayTiepNhan(created).hanTiepNhan(created.plusHours(48)).hanSLA(created.plusHours(48)).build();
   if(i==2) {ticket.setNgayPhanHoiDau(created.plusHours(1));ticket.setNgayDong(created.plusHours(2));}
   ids.add(ticketRepository.saveAndFlush(ticket).getMaYeuCau());
  }
  var ordered=tickets.getAdvisorAssignedTickets(advisor.getMaCV()).stream()
    .map(com.example.TuVanSinhVien.entity.YeuCauTuVan::getMaYeuCau).filter(ids::contains).toList();
  assertEquals(java.util.List.of(ids.get(1),ids.get(0),ids.get(2)),ordered);
 }
 @Test @Transactional void newAnonymousQuestionAndFollowUpArePublicWithoutIdentityOrInternalFields() {
  var student=students.findAll().getFirst();
  var field=fields.findAll().stream().filter(f->"HoatDong".equals(f.getTrangThai()) && Boolean.TRUE.equals(f.getDonVi().getHoatDong())).findFirst().orElseThrow();
  var title="FORUM_TEST_"+java.util.UUID.randomUUID();
  var ticket=tickets.createTicket(student,field.getMaLinhVuc(),field.getDonVi().getMaDonVi(),title,"Nội dung công khai dùng để kiểm thử diễn đàn.",true);
  var publicView=forum.detail(ticket.getMaYeuCau());
  assertEquals("Sinh viên ẩn danh",publicView.get("authorName"));
  assertTrue(forum.isOwner(ticket.getMaYeuCau(),student.getTaiKhoan().getMaTK()));
  assertFalse(forum.isOwner(ticket.getMaYeuCau(),null));
  assertFalse(forum.isOwner(ticket.getMaYeuCau(),advisors.findAll().getFirst().getTaiKhoan().getMaTK()));
  assertFalse(publicView.containsKey("email"));
  assertFalse(publicView.containsKey("maSV"));
  assertEquals(1L,((Number)publicView.get("views")).longValue());
  assertEquals(2L,((Number)forum.detail(ticket.getMaYeuCau()).get("views")).longValue());
  assertEquals(1,forum.list(title,0,20,"latest").size());
  jdbc.update("INSERT INTO dbo.PhanHoiTuVan(MaYeuCau,MaTK_Gui,LoaiPhanHoi,NoiDungTraLoi,NgayTraLoi,DeXuatFAQ) VALUES (?,?,'HoiThem',N'Câu hỏi bổ sung được công khai.',GETDATE(),0)",ticket.getMaYeuCau(),student.getTaiKhoan().getMaTK());
  assertEquals("Sinh viên ẩn danh",forum.replies(ticket.getMaYeuCau()).getFirst().get("authorName"));
  jdbc.update("UPDATE dbo.CauHoiDienDan SET HienThi=0 WHERE MaYeuCau=?",ticket.getMaYeuCau());
  assertEquals(0,forum.count(title));
  assertTrue(forum.replies(ticket.getMaYeuCau()).isEmpty());
  assertThrows(ResponseStatusException.class,()->forum.detail(ticket.getMaYeuCau()));
 }
 @Test void guestsCanReadForumAndHomepageWithoutLoginAndTemplatesEscapeSearch() throws Exception {
  var client=HttpClient.newHttpClient();
  for(String path:new String[]{"/","/forum","/forum?sort=views&page=999999"}) {
   var response=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+path)).GET().build(),HttpResponse.BodyHandlers.ofString());
   assertEquals(200,response.statusCode(),path+" "+response.body().substring(0,Math.min(300,response.body().length())));
   assertTrue(response.body().contains("Diễn đàn"));
  }
  var response=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/forum?kw=%3Cscript%3Ealert(1)%3C%2Fscript%3E")).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(200,response.statusCode());
  assertFalse(response.body().contains("<script>alert(1)</script>"));
  assertTrue(response.body().contains("&lt;script&gt;"));
  var first=forum.list(null,0,1,"latest");
  if(!first.isEmpty()) {
   var detail=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/forum/"+first.getFirst().get("id"))).GET().build(),HttpResponse.BodyHandlers.ofString());
   assertEquals(200,detail.statusCode());
   assertTrue(detail.body().contains("Trao đổi và trả lời"));
   assertFalse(detail.body().contains("MaTK_Gui"));
  }
  var missing=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/forum/2147483647")).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(404,missing.statusCode());
 }
}
