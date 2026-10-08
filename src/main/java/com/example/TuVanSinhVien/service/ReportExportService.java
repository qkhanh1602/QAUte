package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportExportService {
    private final YeuCauTuVanRepository ticketRepo;

    public ReportExportService(YeuCauTuVanRepository ticketRepo) { this.ticketRepo = ticketRepo; }

    public byte[] exportExcel(LocalDate from, LocalDate to, Integer unitId) {
        List<YeuCauTuVan> tickets = load(from, to, unitId);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("BaoCao");
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont(); headerFont.setBold(true); headerStyle.setFont(headerFont);
            String[] headers = {"Mã phiếu", "Ngày tạo", "Sinh viên", "Đơn vị", "Lĩnh vực", "Trạng thái", "Cố vấn"};
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) { header.createCell(i).setCellValue(headers[i]); header.getCell(i).setCellStyle(headerStyle); }
            int rowIndex = 1;
            for (YeuCauTuVan ticket : tickets) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(ticket.getMaYeuCau());
                row.createCell(1).setCellValue(ticket.getNgayTao().toString());
                row.createCell(2).setCellValue(ticket.getSinhVien().getTaiKhoan().getHoTen());
                row.createCell(3).setCellValue(ticket.getDonVi().getTenDonVi());
                row.createCell(4).setCellValue(ticket.getLinhVuc().getTenLinhVuc());
                row.createCell(5).setCellValue(ticket.getTrangThai());
                row.createCell(6).setCellValue(ticket.getCoVanPhuTrach() == null ? "" : ticket.getCoVanPhuTrach().getTaiKhoan().getHoTen());
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            Map<String, Long> counts = countByStatus(tickets);
            int summaryStart = Math.max(rowIndex + 2, 4);
            org.apache.poi.ss.usermodel.Row summaryHeader = sheet.createRow(summaryStart); summaryHeader.createCell(0).setCellValue("Trạng thái"); summaryHeader.createCell(1).setCellValue("Số lượng");
            int cursor = summaryStart + 1;
            for (Map.Entry<String, Long> entry : counts.entrySet()) { org.apache.poi.ss.usermodel.Row row = sheet.createRow(cursor++); row.createCell(0).setCellValue(entry.getKey()); row.createCell(1).setCellValue(entry.getValue()); }
            XSSFDrawing drawing = sheet.createDrawingPatriarch();
            var anchor = drawing.createAnchor(0, 0, 0, 0, 8, summaryStart, 16, summaryStart + 15);
            var chart = drawing.createChart(anchor); chart.setTitleText("Số lượng phiếu theo trạng thái");
            XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
            XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT); leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);
            XDDFDataSource<String> categories = XDDFDataSourcesFactory.fromStringCellRange(sheet, new org.apache.poi.ss.util.CellRangeAddress(summaryStart + 1, cursor - 1, 0, 0));
            XDDFNumericalDataSource<Double> values = XDDFDataSourcesFactory.fromNumericCellRange(sheet, new org.apache.poi.ss.util.CellRangeAddress(summaryStart + 1, cursor - 1, 1, 1));
            XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis); data.addSeries(categories, values); chart.plot(data);
            workbook.write(output); return output.toByteArray();
        } catch (Exception ex) { throw new IllegalStateException("Không thể tạo báo cáo Excel.", ex); }
    }

    public byte[] exportPdf(LocalDate from, LocalDate to, Integer unitId) {
        List<YeuCauTuVan> tickets = load(from, to, unitId);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 32, 32, 32, 42);
            PdfWriter writer = PdfWriter.getInstance(document, output); document.open();
            BaseFont base = PdfFonts.vietnamese();
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(base, 16, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font bodyFont = new com.lowagie.text.Font(base, 9);
            Paragraph title = new Paragraph("BÁO CÁO HOẠT ĐỘNG TƯ VẤN SINH VIÊN QAUTE", titleFont); title.setAlignment(Element.ALIGN_CENTER); document.add(title);
            Paragraph period = new Paragraph("Khoảng thời gian: " + (from == null ? "Tất cả" : from) + " đến " + (to == null ? "Hiện tại" : to), bodyFont); period.setSpacingAfter(12); document.add(period);
            Map<String, Long> counts = countByStatus(tickets);
            PdfContentByte canvas = writer.getDirectContent(); float chartX = 55, chartY = 430, maxWidth = 280; long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(1);
            int bar = 0; for (Map.Entry<String, Long> entry : counts.entrySet()) { float width = maxWidth * entry.getValue() / max; canvas.setColorFill(new Color(22,101,52)); canvas.rectangle(chartX, chartY - bar * 24, width, 14); canvas.fill(); canvas.beginText(); canvas.setFontAndSize(base, 9); canvas.setColorFill(Color.BLACK); canvas.setTextMatrix(chartX + maxWidth + 10, chartY - bar * 24 + 2); canvas.showText(entry.getKey() + ": " + entry.getValue()); canvas.endText(); bar++; }
            document.add(new Paragraph(" ")); document.add(new Paragraph(" ")); document.add(new Paragraph(" ")); document.add(new Paragraph(" ")); document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[]{1, 2, 3, 3, 3, 2}); table.setWidthPercentage(100);
            for (String header : List.of("Mã", "Ngày tạo", "Sinh viên", "Đơn vị", "Lĩnh vực", "Trạng thái")) { PdfPCell cell = new PdfPCell(new Phrase(header, bodyFont)); cell.setBackgroundColor(new Color(220, 232, 224)); table.addCell(cell); }
            for (YeuCauTuVan ticket : tickets) { add(table, bodyFont, String.valueOf(ticket.getMaYeuCau())); add(table, bodyFont, ticket.getNgayTao().toLocalDate().toString()); add(table, bodyFont, ticket.getSinhVien().getTaiKhoan().getHoTen()); add(table, bodyFont, ticket.getDonVi().getTenDonVi()); add(table, bodyFont, ticket.getLinhVuc().getTenLinhVuc()); add(table, bodyFont, ticket.getTrangThai()); }
            document.add(table);
            Paragraph signature = new Paragraph("\nNgười lập báo cáo                                      Xác nhận của đơn vị", bodyFont); signature.setAlignment(Element.ALIGN_RIGHT); document.add(signature);
            document.close(); return output.toByteArray();
        } catch (Exception ex) { throw new IllegalStateException("Không thể tạo báo cáo PDF.", ex); }
    }

    private void add(PdfPTable table, com.lowagie.text.Font font, String text) { table.addCell(new PdfPCell(new Phrase(text == null ? "" : text, font))); }
    private List<YeuCauTuVan> load(LocalDate from, LocalDate to, Integer unitId) { LocalDateTime fromTime = from == null ? null : from.atStartOfDay(); LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay(); return ticketRepo.findForReport(fromTime, toTime, unitId); }
    private Map<String, Long> countByStatus(List<YeuCauTuVan> tickets) { Map<String, Long> result = new LinkedHashMap<>(); for (String state : List.of("ChoTiepNhan", "DangXuLy", "DaPhanHoi", "Dong")) result.put(state, tickets.stream().filter(t -> state.equals(t.getTrangThai())).count()); return result; }
}
