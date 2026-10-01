package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.entity.PhanHoiTuVan;
import com.example.TuVanSinhVien.entity.PhienChat;
import com.example.TuVanSinhVien.entity.TepDinhKem;
import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import com.example.TuVanSinhVien.service.AttachmentService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping("/attachments/{id}")
    @Transactional(readOnly = true)
    public String download(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails user) {
        TepDinhKem attachment = attachmentService.getRequired(id);
        if (attachment.getBaiVietHuongDan() != null && "XuatBan".equals(attachment.getBaiVietHuongDan().getTrangThai())) {
            return "redirect:" + attachmentService.createSignedUrl(attachment);
        }
        if (user == null) throw new SecurityException("Vui lòng đăng nhập để xem tệp.");
        if (!canAccess(attachment, user)) throw new SecurityException("Bạn không có quyền xem tệp này.");
        return "redirect:" + attachmentService.createSignedUrl(attachment);
    }

    private boolean canAccess(TepDinhKem attachment, CustomUserDetails user) {
        if ("Admin".equals(user.getVaiTro()) || attachment.getTaiKhoanTaiLen().getMaTK().equals(user.getMaTK())) return true;
        YeuCauTuVan ticket = attachment.getYeuCauTuVan();
        PhanHoiTuVan reply = attachment.getPhanHoiTuVan();
        if (ticket == null && reply != null) ticket = reply.getYeuCauTuVan();
        if (ticket != null) {
            boolean owner = ticket.getSinhVien().getTaiKhoan().getMaTK().equals(user.getMaTK());
            boolean advisor = ticket.getCoVanPhuTrach() != null && ticket.getCoVanPhuTrach().getTaiKhoan().getMaTK().equals(user.getMaTK());
            return owner || advisor;
        }
        if (attachment.getTinNhanChat() != null) {
            PhienChat chat = attachment.getTinNhanChat().getPhienChat();
            boolean student = chat.getSinhVien().getTaiKhoan().getMaTK().equals(user.getMaTK());
            boolean advisor = chat.getNguoiTuVan() != null && chat.getNguoiTuVan().getTaiKhoan().getMaTK().equals(user.getMaTK());
            return student || advisor;
        }
        return false;
    }
}
