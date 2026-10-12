package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.controller.AttachmentController;
import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.service.AttachmentService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AttachmentDeliveryTests {
 @Test void legacyPdfIsServedWithCorrectMimeFilenameAndNoStore() {
  var files=mock(AttachmentService.class); var attachment=new TepDinhKem();
  attachment.setTenGoc("legacy_file"); attachment.setMimeType("application/pdf");
  when(files.getRequired(4L)).thenReturn(attachment);
  byte[] bytes="%PDF-1.4".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
  when(files.downloadContent(attachment)).thenReturn(bytes);
  var account=mock(TaiKhoan.class); when(account.getVaiTro()).thenReturn("Admin");
  var response=new AttachmentController(files).download(4L,new CustomUserDetails(account));
  assertEquals("application/pdf",response.getHeaders().getContentType().toString());
  assertTrue(response.getHeaders().getFirst("Content-Disposition").contains("legacy_file.pdf"));
  assertEquals("no-store",response.getHeaders().getCacheControl());
  assertArrayEquals(bytes,response.getBody());
 }
 @Test void privateFileIsNeverFetchedBeforeAuthorization() {
  var files=mock(AttachmentService.class); var attachment=new TepDinhKem();
  var uploader=mock(TaiKhoan.class); when(uploader.getMaTK()).thenReturn(1); attachment.setTaiKhoanTaiLen(uploader);
  when(files.getRequired(4L)).thenReturn(attachment);
  var controller=new AttachmentController(files);
  assertThrows(SecurityException.class,()->controller.download(4L,null));
  var stranger=mock(TaiKhoan.class); when(stranger.getVaiTro()).thenReturn("SinhVien"); when(stranger.getMaTK()).thenReturn(2);
  assertThrows(SecurityException.class,()->controller.download(4L,new CustomUserDetails(stranger)));
  verify(files,never()).downloadContent(any());
 }
}
