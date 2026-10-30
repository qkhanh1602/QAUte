package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportExportRegressionTests {
    @Test void coldWorkbookGenerationWorksWith32MbAndNoPoiClasspath() throws Exception {
        String javaBinary = java.nio.file.Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = "target/classes" + java.io.File.pathSeparator + "target/test-classes";
        var process = new ProcessBuilder(javaBinary, "-Xmx32m", "-cp", classpath, ColdWorkbook.class.getName())
                .redirectErrorStream(true).start();
        assertTrue(process.waitFor(15, java.util.concurrent.TimeUnit.SECONDS), "Cold export timed out");
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        assertEquals(0, process.exitValue(), output);
        assertTrue(output.contains("XLSX_OK"), output);
    }

    public static class ColdWorkbook {
        public static void main(String[] args) {
            var row = new ReportRow("Tư vấn học phí", null, "Sinh viên", "CNTT", "Học tập", "Dong", null);
            byte[] bytes = ReportWorkbook.create(java.util.Collections.nCopies(1000, row), java.util.Map.of("Dong", 1000L));
            if (bytes[0] != 'P' || bytes[1] != 'K') throw new IllegalStateException("Invalid ZIP");
            System.out.println("XLSX_OK " + bytes.length);
        }
    }

    @Test void workbookPreservesUnicodeTextNullAdvisorAndChart() throws Exception {
        var repo = mock(YeuCauTuVanRepository.class);
        var day = LocalDate.of(2026, 10, 16);
        when(repo.findReportRows(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), 1))
                .thenReturn(List.of(new ReportRow("=1+1 <Tư vấn> & học phí\u0001", day.atTime(23, 59),
                        "Nguyễn Văn An", "CNTT", "Học tập", "Dong", null)));
        byte[] bytes = new ReportExportService(repo).exportExcel(day, day, 1);
        verify(repo).findReportRows(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), 1);
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            var sheet = workbook.getSheetAt(0);
            assertEquals("Tiêu đề câu hỏi", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals(CellType.STRING, sheet.getRow(1).getCell(0).getCellType());
            assertEquals("=1+1 <Tư vấn> & học phí", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("16/10/2026 23:59", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Nguyễn Văn An", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("", sheet.getRow(1).getCell(6).getStringCellValue());
            assertEquals(1d, sheet.getRow(9).getCell(1).getNumericCellValue());
            assertTrue(sheet.getPaneInformation().isFreezePane());
            var errors = new java.util.ArrayList<org.apache.xmlbeans.XmlError>();
            assertTrue(sheet.getCTWorksheet().validate(new org.apache.xmlbeans.XmlOptions().setErrorListener(errors)), errors.toString());
            assertTrue(workbook.getFontAt(sheet.getRow(0).getCell(0).getCellStyle().getFontIndex()).getBold());
            var chart = sheet.getDrawingPatriarch().getCharts().getFirst();
            assertEquals("Số lượng câu hỏi theo trạng thái", chart.getTitleText().getString());
            var series = chart.getCTChart().getPlotArea().getBarChartArray(0).getSerArray(0);
            assertEquals(4, series.getCat().getStrRef().getStrCache().sizeOfPtArray());
            assertEquals("1", series.getVal().getNumRef().getNumCache().getPtArray(3).getV());
        }
    }

    @Test void emptyExportIsReadableWithZeroCounts() throws Exception {
        var repo = mock(YeuCauTuVanRepository.class);
        when(repo.findReportRows(null, null, null)).thenReturn(List.of());
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(new ReportExportService(repo).exportExcel(null, null, null)))) {
            assertEquals(0d, workbook.getSheetAt(0).getRow(5).getCell(1).getNumericCellValue());
            assertEquals(1, workbook.getSheetAt(0).getDrawingPatriarch().getCharts().size());
        }
    }

    @Test void invalidFiltersNeverAcquireDatabaseConnection() {
        var repo = mock(YeuCauTuVanRepository.class);
        var service = new ReportExportService(repo);
        assertThrows(IllegalArgumentException.class, () -> service.exportExcel(LocalDate.now(), LocalDate.now().minusDays(1), null));
        assertThrows(IllegalArgumentException.class, () -> service.exportPdf(null, LocalDate.MAX, null));
        assertThrows(IllegalArgumentException.class, () -> service.exportExcel(null, null, -1));
        verifyNoInteractions(repo);
    }
}
