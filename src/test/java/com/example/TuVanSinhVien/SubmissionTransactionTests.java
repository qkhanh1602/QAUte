package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.*;
import com.example.TuVanSinhVien.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.Map;

@SpringBootTest(properties="app.workflow.scheduling.enabled=false")
class SubmissionTransactionTests {
    @Autowired SubmissionService submissions;
    @Autowired SinhVienRepository students;
    @Autowired LinhVucTuVanRepository fields;
    @Autowired PlatformTransactionManager transactions;
    @Autowired JdbcTemplate jdbc;
    @Autowired EmailOutboxService outbox;
    @MockitoBean com.cloudinary.Cloudinary cloudinary;
    @MockitoBean TepDinhKemRepository attachments;
    @MockitoBean NotificationService notifications;
    @MockitoBean EmailService email;

    @Test void metadataFailureRollsBackTicketAndDeletesUploadedAsset() throws Exception {
        var uploader=mock(com.cloudinary.Uploader.class);
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class),anyMap())).thenReturn(Map.of("asset_id","test-asset","public_id","qaute/test-rollback"));
        when(attachments.save(any())).thenThrow(new org.springframework.dao.DataIntegrityViolationException("simulated metadata failure"));
        var file=new org.springframework.mock.web.MockMultipartFile("attachment","proof.pdf","application/pdf","%PDF-1.4\nTest payload".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String title="TEST_ROLLBACK_"+java.util.UUID.randomUUID();
        var tx=new TransactionTemplate(transactions);
        assertThrows(org.springframework.dao.DataIntegrityViolationException.class,()->tx.execute(status->{
            var student=students.findAll().getFirst();
            var field=fields.findAll().stream().filter(f->"HoatDong".equals(f.getTrangThai()) && Boolean.TRUE.equals(f.getDonVi().getHoatDong())).findFirst().orElseThrow();
            return submissions.createTicket(student,field.getMaLinhVuc(),field.getDonVi().getMaDonVi(),title,"Nội dung đủ dài để kiểm thử rollback giao dịch.",false,file);
        }));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM dbo.YeuCauTuVan WHERE TieuDe=?",Integer.class,title));
        verify(uploader).destroy(eq("qaute/test-rollback"),anyMap());
        var uploadOptions=org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class),uploadOptions.capture());
        assertEquals("raw",uploadOptions.getValue().get("resource_type"));
        assertEquals("authenticated",uploadOptions.getValue().get("type"));
        assertTrue(uploadOptions.getValue().get("public_id").toString().endsWith(".pdf"));
    }

    @Test void outboxQueueIsDurableAndDeduplicatedInSqlServer() {
        String key="TEST_OUTBOX_"+java.util.UUID.randomUUID();
        try {
            outbox.queue(key,"probe@example.test","Test","Body");
            outbox.queue(key,"probe@example.test","Test","Body");
            assertEquals(1,jdbc.queryForObject("SELECT COUNT(*) FROM dbo.HangDoiSuKien WHERE KhoaChongLap=? AND TrangThai='ChoXuLy' AND ISJSON(Payload)=1",Integer.class,key));
            verifyNoInteractions(email);
        } finally { jdbc.update("DELETE FROM dbo.HangDoiSuKien WHERE KhoaChongLap=?",key); }
    }
}
