package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.PhanHoiTuVan;
import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import com.example.TuVanSinhVien.repository.YeuCauTuVanRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;

@Service
@Transactional(readOnly = true)
public class ConsultationRecordPdfService {
    private final YeuCauTuVanRepository ticketRepo;

    public ConsultationRecordPdfService(YeuCauTuVanRepository ticketRepo) {
        this.ticketRepo = ticketRepo;
    }

    public byte[] exportTicket(YeuCauTuVan ticket) {
        if (ticket == null) return new byte[0];
        if (ticket.getMaYeuCau() != null) {
            ticket = ticketRepo.findById(ticket.getMaYeuCau()).orElse(ticket);
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 42, 42, 42, 42);
            PdfWriter.getInstance(document, output);
            document.open();
            BaseFont base = PdfFonts.vietnamese();
            Font title = new Font(base, 16, Font.BOLD);
            Font heading = new Font(base, 11, Font.BOLD);
            Font body = new Font(base, 10);
            Paragraph name = new Paragraph("BIÊN BẢN KẾT QUẢ TƯ VẤN SINH VIÊN", title);
            name.setAlignment(Element.ALIGN_CENTER); name.setSpacingAfter(16); document.add(name);
            add(document, body, "Mã phiếu: #" + ticket.getMaYeuCau());
            add(document, body, "Sinh viên: " + ticket.getSinhVien().getTaiKhoan().getHoTen() + " - " + ticket.getSinhVien().getMssv());
            add(document, body, "Đơn vị: " + ticket.getDonVi().getTenDonVi());
            add(document, body, "Lĩnh vực: " + ticket.getLinhVuc().getTenLinhVuc());
            add(document, body, "Trạng thái: " + ticket.getTrangThai());
            add(document, body, "Ngày tạo: " + ticket.getNgayTao());
            section(document, heading, "Câu hỏi: " + ticket.getTieuDe());
            add(document, body, ticket.getNoiDung());
            section(document, heading, "Nội dung trao đổi");
            if (ticket.getPhanHoiList().isEmpty()) add(document, body, "Chưa có phản hồi.");
            for (PhanHoiTuVan reply : ticket.getPhanHoiList()) {
                add(document, heading, reply.getTaiKhoanGui().getHoTen() + " - " + reply.getNgayTraLoi());
                add(document, body, reply.getNoiDungTraLoi());
            }
            section(document, heading, "Kết luận");
            add(document, body, ticket.getKetLuan() == null || ticket.getKetLuan().isBlank() ? "Chưa có kết luận." : ticket.getKetLuan());
            Paragraph signatures = new Paragraph("\nSinh viên xác nhận                                  Cố vấn xác nhận", body);
            signatures.setAlignment(Element.ALIGN_CENTER); document.add(signatures);
            document.close();
            return output.toByteArray();
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo biên bản tư vấn PDF.", ex);
        }
    }

    private void section(Document document, Font font, String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text, font); paragraph.setSpacingBefore(12); paragraph.setSpacingAfter(5); document.add(paragraph);
    }
    private void add(Document document, Font font, String text) throws DocumentException {
        Paragraph paragraph = new Paragraph(text == null ? "" : text, font); paragraph.setSpacingAfter(4); document.add(paragraph);
    }
}
