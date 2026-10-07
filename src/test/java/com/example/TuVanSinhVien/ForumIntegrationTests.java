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
 @LocalServerPort int port;

 @Test @Transactional void newAnonymousQuestionAndFollowUpArePublicWithoutIdentityOrInternalFields() {
  var student=students.findAll().getFirst();
  var field=fields.findAll().stream().filter(f->"HoatDong".equals(f.getTrangThai()) && Boolean.TRUE.equals(f.getDonVi().getHoatDong())).findFirst().orElseThrow();
  var title="FORUM_TEST_"+java.util.UUID.randomUUID();
  var ticket=tickets.createTicket(student,field.getMaLinhVuc(),field.getDonVi().getMaDonVi(),title,"Nội dung công khai dùng để kiểm thử diễn đàn.",true);
  var publicView=forum.detail(ticket.getMaYeuCau());
  assertEquals("Sinh viên ẩn danh",publicView.get("authorName"));
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
