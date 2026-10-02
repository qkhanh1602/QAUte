package com.example.TuVanSinhVien;
import com.example.TuVanSinhVien.service.ReportExportService;
import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class PdfUnicodeTest {
    @Test void portablePdfPreservesVietnamese() throws Exception {
        var repo=mock(YeuCauTuVanRepository.class);
        var bytes=new ReportExportService(repo).exportPdf(null,null,null);
        try(var reader=new PdfReader(bytes)) {
            String text=new PdfTextExtractor(reader).getTextFromPage(1);
            assertTrue(text.contains("BÁO CÁO HOẠT ĐỘNG TƯ VẤN SINH VIÊN QAUTE"));
            assertTrue(text.contains("Người lập báo cáo"));
        }
    }
}
