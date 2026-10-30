package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.nio.file.*;
import java.net.*;
import java.net.http.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"app.workflow.scheduling.enabled=false","spring.jpa.open-in-view=false"})
class ExportIntegrationTests {
 @Autowired ReportExportService reports;
 @Autowired ConsultationRecordPdfService records;
 @Autowired YeuCauTuVanRepository tickets;
 @Autowired TepDinhKemRepository attachments;
 @Autowired AttachmentService files;
 @Autowired TaiKhoanRepository accounts;
 @Autowired JwtService jwt;
 @Autowired org.springframework.transaction.PlatformTransactionManager transactions;
 @org.springframework.boot.test.web.server.LocalServerPort int port;

 @Test void reportPdfLoadsLazyRelationshipsWithoutOpenSessionInView() throws Exception {
  var bytes=reports.exportPdf(null,null,null);
  try(var pdf=new PdfReader(bytes)) {
   assertTrue(new PdfTextExtractor(pdf).getTextFromPage(1).contains("BÁO CÁO HOẠT ĐỘNG"));
  }
  save("report.pdf",bytes);
 }
 @Test void reportExcelLoadsLazyRelationshipsWithoutOpenSessionInView() throws Exception {
  try(var workbook=new org.apache.poi.xssf.usermodel.XSSFWorkbook(new java.io.ByteArrayInputStream(reports.exportExcel(null,null,null)))) {
   assertEquals("Sinh viên",workbook.getSheetAt(0).getRow(0).getCell(2).getStringCellValue());
   assertTrue(workbook.getSheetAt(0).getLastRowNum()>0);
  }
 }
 @Test void forbiddenAndInvalidExportRenderFriendlyPagesThroughRealFiltersAndJsp() throws Exception {
  var client=HttpClient.newBuilder().cookieHandler(new java.net.CookieManager()).build();
  var login=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/login")).GET().build(),HttpResponse.BodyHandlers.ofString());
  var csrf=java.util.regex.Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(login.body());
  assertTrue(csrf.find(), "Login form must contain CSRF token");
  String form="username=covan.cntt&password=Covan%40123&_csrf="+java.net.URLEncoder.encode(csrf.group(1), java.nio.charset.StandardCharsets.UTF_8);
  var signedIn=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/login"))
    .header("Content-Type","application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(form)).build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(302,signedIn.statusCode());
  assertFalse(signedIn.headers().firstValue("Location").orElse("").contains("error"));
  var denied=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/admin/dashboard"))
    .GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(403,denied.statusCode());
  assertTrue(denied.body().contains("Bạn không có quyền truy cập"), denied.body());
  assertTrue(denied.body().contains("/advisor/dashboard"));
  var admin=accounts.findByTenDangNhap("admin").orElseThrow();
  var invalid=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/admin/reports/export?from=2026-10-17&to=2026-10-01"))
    .header("Authorization","Bearer "+jwt.createAccessToken(admin)).GET().build(),HttpResponse.BodyHandlers.ofString());
  assertEquals(400,invalid.statusCode());
  assertTrue(invalid.body().contains("Ngày bắt đầu phải nhỏ hơn"));
 }
 @Test void detachedTicketIsReloadedBeforeRenderingRecord() throws Exception {
  var ticket=tickets.findAll().getFirst();
  assertFalse(org.hibernate.Hibernate.isInitialized(ticket.getSinhVien()));
  var bytes=records.exportTicket(ticket);
  try(var pdf=new PdfReader(bytes)) {
   assertTrue(new PdfTextExtractor(pdf).getTextFromPage(1).contains("BIÊN BẢN KẾT QUẢ"));
  }
  save("record.pdf",bytes);
 }
 @Test @EnabledIfSystemProperty(named="qaute.cloudinary.read-test",matches="true")
 void existingCloudinaryPdfCanBeDownloadedAndParsed() throws Exception {
  var attachment=attachments.findAll().stream().filter(a->"application/pdf".equals(a.getMimeType())).findFirst().orElseThrow();
  var client=HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(20)).build();
  var response=client.send(HttpRequest.newBuilder(URI.create(files.createSignedUrl(attachment))).timeout(java.time.Duration.ofSeconds(45)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
  System.out.println("CLOUDINARY_PDF_STATUS="+response.statusCode()+" MIME="+response.headers().firstValue("Content-Type").orElse("")+" BYTES="+response.body().length);
  assertEquals(200,response.statusCode());
  assertEquals("%PDF",new String(response.body(),0,4,java.nio.charset.StandardCharsets.US_ASCII));
  try(var pdf=new PdfReader(response.body())) { assertTrue(pdf.getNumberOfPages()>0); }
  var uploader=accounts.findById(attachment.getTaiKhoanTaiLen().getMaTK()).orElseThrow();
  var appResponse=client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/attachments/"+attachment.getMaTep()))
    .header("Authorization","Bearer "+jwt.createAccessToken(uploader)).GET().build(),HttpResponse.BodyHandlers.ofByteArray());
  assertEquals(200,appResponse.statusCode());
  assertTrue(appResponse.headers().firstValue("Content-Type").orElse("").startsWith("application/pdf"));
  assertTrue(appResponse.headers().firstValue("Content-Disposition").orElse("").contains(".pdf"));
  assertArrayEquals(response.body(),appResponse.body());
  save("existing-attachment.pdf",response.body());
  // Verify actual upload preserves PDF bytes and extension; rollback removes DB row and cloud asset.
  byte[] fresh=reports.exportPdf(null,null,null);
  new org.springframework.transaction.support.TransactionTemplate(transactions).execute(status->{
   try {
    var ticket=tickets.findAll().getFirst();
    var file=new org.springframework.mock.web.MockMultipartFile("attachment","qaute-report.pdf","application/pdf",fresh);
    var uploaded=files.attachToTicket(file,ticket.getSinhVien().getTaiKhoan(),ticket);
    assertTrue(uploaded.getCloudinaryPublicId().endsWith(".pdf"));
    assertArrayEquals(fresh,files.downloadContent(uploaded));
    System.out.println("CLOUDINARY_PDF_UPLOAD_ROUNDTRIP_OK bytes="+fresh.length);
    return null;
   } finally { status.setRollbackOnly(); }
  });
 }
 private void save(String name,byte[] bytes) throws Exception {
  String directory=System.getProperty("qaute.pdf-review-dir");
  if(directory!=null) { Files.createDirectories(Path.of(directory)); Files.write(Path.of(directory,name),bytes); }
 }
}
