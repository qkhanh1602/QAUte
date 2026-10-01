package com.example.TuVanSinhVien.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.TepDinhKemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AttachmentService {
    private final Cloudinary cloudinary;
    private final FileValidationService validator;
    private final TepDinhKemRepository attachmentRepo;

    public AttachmentService(Cloudinary cloudinary, FileValidationService validator, TepDinhKemRepository attachmentRepo) {
        this.cloudinary = cloudinary; this.validator = validator; this.attachmentRepo = attachmentRepo;
    }

    @Transactional
    public TepDinhKem attachToTicket(MultipartFile file, TaiKhoan uploader, YeuCauTuVan ticket) {
        TepDinhKem attachment = upload(file, uploader); attachment.setYeuCauTuVan(ticket); return attachmentRepo.save(attachment);
    }

    @Transactional
    public TepDinhKem attachToReply(MultipartFile file, TaiKhoan uploader, PhanHoiTuVan reply) {
        TepDinhKem attachment = upload(file, uploader); attachment.setPhanHoiTuVan(reply); return attachmentRepo.save(attachment);
    }

    @Transactional
    public TepDinhKem attachToMessage(MultipartFile file, TaiKhoan uploader, TinNhanChat message) {
        TepDinhKem attachment = upload(file, uploader); attachment.setTinNhanChat(message); return attachmentRepo.save(attachment);
    }

    @Transactional
    public TepDinhKem attachToArticle(MultipartFile file, TaiKhoan uploader, BaiVietHuongDan article) {
        TepDinhKem attachment = upload(file, uploader); attachment.setBaiVietHuongDan(article); return attachmentRepo.save(attachment);
    }

    private TepDinhKem upload(MultipartFile file, TaiKhoan uploader) {
        FileValidationService.FileValidationResult validation = validator.validateFile(file);
        if (!validation.isValid()) throw new IllegalArgumentException(validation.getMessage());
        try {
            String resourceType = file.getContentType() != null && file.getContentType().startsWith("image/") ? "image" : "raw";
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "qaute/attachments", "resource_type", resourceType, "type", "authenticated",
                    "use_filename", true, "unique_filename", true));
            TepDinhKem attachment = new TepDinhKem();
            attachment.setTaiKhoanTaiLen(uploader); attachment.setTenGoc(validation.getSafeFileName());
            attachment.setMimeType(file.getContentType()); attachment.setDungLuong(file.getSize());
            attachment.setCloudinaryAssetId(String.valueOf(result.get("asset_id")));
            attachment.setCloudinaryPublicId(String.valueOf(result.get("public_id")));
            attachment.setResourceType(resourceType); attachment.setDeliveryType("authenticated");
            attachment.setNgayTao(LocalDateTime.now()); return attachment;
        } catch (Exception ex) { throw new IllegalStateException("Không thể tải tệp lên Cloudinary: " + ex.getMessage(), ex); }
    }

    public List<TepDinhKem> getTicketAttachments(Integer ticketId) { return attachmentRepo.findByYeuCauTuVan_MaYeuCauOrderByNgayTaoAsc(ticketId); }
    public List<TepDinhKem> getReplyAttachments(Integer replyId) { return attachmentRepo.findByPhanHoiTuVan_MaPhanHoiOrderByNgayTaoAsc(replyId); }
    public List<TepDinhKem> getMessageAttachments(Long messageId) { return attachmentRepo.findByTinNhanChat_MaTinNhanOrderByNgayTaoAsc(messageId); }
    public List<TepDinhKem> getArticleAttachments(Integer articleId) { return attachmentRepo.findByBaiVietHuongDan_MaBaiVietOrderByNgayTaoAsc(articleId); }
    public TepDinhKem getRequired(Long id) { return attachmentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp.")); }
    public String createSignedUrl(TepDinhKem attachment) {
        return cloudinary.url().secure(true).signed(true).resourceType(attachment.getResourceType())
                .type(attachment.getDeliveryType()).generate(attachment.getCloudinaryPublicId());
    }
}
