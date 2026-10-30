package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportExportService {
    private final YeuCauTuVanRepository ticketRepo;

    public ReportExportService(YeuCauTuVanRepository ticketRepo) { this.ticketRepo = ticketRepo; }

    public byte[] exportExcel(LocalDate from, LocalDate to, Integer unitId) {
        List<ReportRow> tickets = load(from, to, unitId);
        return ReportWorkbook.create(tickets, countByStatus(tickets));
    }

    public byte[] exportPdf(LocalDate from, LocalDate to, Integer unitId) {
        List<ReportRow> tickets = load(from, to, unitId);
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 32, 32, 32, 42);
            PdfWriter writer = PdfWriter.getInstance(document, output); document.open();
            BaseFont base = PdfFonts.vietnamese();
            com.lowagie.text.Font titleFont = new com.lowagie.text.Font(base, 16, com.lowagie.text.Font.BOLD);
            com.lowagie.text.Font bodyFont = new com.lowagie.text.Font(base, 9);
            Paragraph title = new Paragraph("BÁO CÁO HOẠT ĐỘNG TƯ VẤN SINH VIÊN QAUTE", titleFont); title.setAlignment(Element.ALIGN_CENTER); document.add(title);
            Paragraph period = new Paragraph("Khoảng thời gian: " + (from == null ? "Tất cả" : from) + " đến " + (to == null ? "Hiện tại" : to), bodyFont); period.setSpacingAfter(12); document.add(period);
            Map<String, Long> counts = countByStatus(tickets);
            var canvas = writer.getDirectContent().createTemplate(700, 120); float chartX = 10, chartY = 100, maxWidth = 280; long max = Math.max(1, counts.values().stream().mapToLong(Long::longValue).max().orElse(1));
            int bar = 0; for (Map.Entry<String, Long> entry : counts.entrySet()) { float width = maxWidth * entry.getValue() / max; canvas.setColorFill(new Color(22,101,52)); canvas.rectangle(chartX, chartY - bar * 24, width, 14); canvas.fill(); canvas.beginText(); canvas.setFontAndSize(base, 9); canvas.setColorFill(Color.BLACK); canvas.setTextMatrix(chartX + maxWidth + 10, chartY - bar * 24 + 2); canvas.showText(entry.getKey() + ": " + entry.getValue()); canvas.endText(); bar++; }
            document.add(com.lowagie.text.Image.getInstance(canvas));
            PdfPTable table = new PdfPTable(new float[]{4, 2, 3, 3, 3, 2}); table.setWidthPercentage(100);
            table.setHeaderRows(1); table.setSpacingBefore(12);
            for (String header : List.of("Tiêu đề", "Ngày tạo", "Sinh viên", "Đơn vị", "Lĩnh vực", "Trạng thái")) { PdfPCell cell = new PdfPCell(new Phrase(header, bodyFont)); cell.setBackgroundColor(new Color(220, 232, 224)); table.addCell(cell); }
            for (ReportRow ticket : tickets) { add(table, bodyFont, ticket.title()); add(table, bodyFont, ticket.createdAt().toLocalDate().toString()); add(table, bodyFont, ticket.studentName()); add(table, bodyFont, ticket.unitName()); add(table, bodyFont, ticket.fieldName()); add(table, bodyFont, ticket.status()); }
            document.add(table);
            Paragraph signature = new Paragraph("\nNgười lập báo cáo                                      Xác nhận của đơn vị", bodyFont); signature.setAlignment(Element.ALIGN_RIGHT); document.add(signature);
            document.close(); return output.toByteArray();
        } catch (Exception ex) { throw new IllegalStateException("Không thể tạo báo cáo PDF.", ex); }
    }

    private void add(PdfPTable table, com.lowagie.text.Font font, String text) { table.addCell(new PdfPCell(new Phrase(text == null ? "" : text, font))); }
    private List<ReportRow> load(LocalDate from, LocalDate to, Integer unitId) {
        if (from != null && to != null && from.isAfter(to)) throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.");
        if (LocalDate.MAX.equals(to)) throw new IllegalArgumentException("Ngày kết thúc không hợp lệ.");
        if (unitId != null && unitId <= 0) throw new IllegalArgumentException("Đơn vị không hợp lệ.");
        LocalDateTime fromTime = from == null ? null : from.atStartOfDay();
        LocalDateTime toTime = to == null ? null : to.plusDays(1).atStartOfDay();
        // Fetch scalar rows in a short repository transaction, then release the connection.
        return ticketRepo.findReportRows(fromTime, toTime, unitId);
    }
    private Map<String, Long> countByStatus(List<ReportRow> tickets) { Map<String, Long> result = new LinkedHashMap<>(); for (String state : List.of("ChoTiepNhan", "DangXuLy", "DaPhanHoi", "Dong")) result.put(state, tickets.stream().filter(t -> state.equals(t.status())).count()); return result; }
}
