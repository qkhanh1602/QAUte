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
    @org.springframework.beans.factory.annotation.Autowired
    private java.time.Clock clock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
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
                    "public_id", "raw".equals(resourceType) ? validation.getSafeFileName() : java.util.UUID.randomUUID().toString(),
                    "use_filename", true, "unique_filename", true));
            String publicId = String.valueOf(result.get("public_id"));
            if (org.springframework.transaction.support.TransactionSynchronizationManager.isSynchronizationActive()) {
                org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override public void afterCompletion(int status) {
                                if (status != STATUS_COMMITTED) {
                                    try { cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType, "type", "authenticated")); }
                                    catch (Exception cleanupError) {
                                        org.slf4j.LoggerFactory.getLogger(AttachmentService.class).error("Không xóa được asset sau rollback: {}", publicId, cleanupError);
                                    }
                                }
                            }
                        });
            }
            TepDinhKem attachment = new TepDinhKem();
            attachment.setTaiKhoanTaiLen(uploader); attachment.setTenGoc(validation.getSafeFileName());
            attachment.setMimeType(file.getContentType()); attachment.setDungLuong(file.getSize());
            attachment.setCloudinaryAssetId(String.valueOf(result.get("asset_id")));
            attachment.setCloudinaryPublicId(String.valueOf(result.get("public_id")));
            attachment.setResourceType(resourceType); attachment.setDeliveryType("authenticated");
            attachment.setNgayTao(LocalDateTime.now(clock)); return attachment;
        } catch (Exception ex) { throw new IllegalStateException("Không thể tải tệp lên Cloudinary: " + ex.getMessage(), ex); }
    }

    public void validateOptional(MultipartFile file) {
        if (file == null || file.isEmpty()) return;
        var result = validator.validateFile(file);
        if (!result.isValid()) throw new IllegalArgumentException(result.getMessage());
    }

    public List<TepDinhKem> getTicketAttachments(Integer ticketId) { return attachmentRepo.findByYeuCauTuVan_MaYeuCauOrderByNgayTaoAsc(ticketId); }
    public List<TepDinhKem> getReplyAttachments(Integer replyId) { return attachmentRepo.findByPhanHoiTuVan_MaPhanHoiOrderByNgayTaoAsc(replyId); }
    public List<TepDinhKem> getMessageAttachments(Long messageId) { return attachmentRepo.findByTinNhanChat_MaTinNhanOrderByNgayTaoAsc(messageId); }
    public List<TepDinhKem> getArticleAttachments(Integer articleId) { return attachmentRepo.findByBaiVietHuongDan_MaBaiVietOrderByNgayTaoAsc(articleId); }
    public TepDinhKem getRequired(Long id) { return attachmentRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp.")); }
    public byte[] downloadContent(TepDinhKem attachment) {
        try {
            var client=java.net.http.HttpClient.newBuilder().connectTimeout(java.time.Duration.ofSeconds(10)).build();
            var request=java.net.http.HttpRequest.newBuilder(java.net.URI.create(createSignedUrl(attachment)))
                    .timeout(java.time.Duration.ofSeconds(30)).GET().build();
            var response=client.send(request,java.net.http.HttpResponse.BodyHandlers.ofInputStream());
            try(var input=response.body()) {
                if(response.statusCode()!=200) throw new IllegalStateException("Kho tệp chưa cho phép tải nội dung (HTTP "+response.statusCode()+").");
                byte[] bytes=input.readNBytes((int)FileValidationService.MAX_FILE_SIZE_BYTES+1);
                if(bytes.length>FileValidationService.MAX_FILE_SIZE_BYTES) throw new IllegalStateException("Nội dung tệp vượt giới hạn tải.");
                if("application/pdf".equalsIgnoreCase(attachment.getMimeType())
                        && (bytes.length<4 || bytes[0]!='%' || bytes[1]!='P' || bytes[2]!='D' || bytes[3]!='F'))
                    throw new IllegalStateException("Nội dung tải về không phải PDF hợp lệ.");
                return bytes;
            }
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt(); throw new IllegalStateException("Tải tệp bị gián đoạn.");
        } catch(java.io.IOException ex) { throw new IllegalStateException("Chưa tải được nội dung tệp. Vui lòng thử lại."); }
    }
    public String createSignedUrl(TepDinhKem attachment) {
        if("raw".equals(attachment.getResourceType())) {
            try {
                return cloudinary.privateDownload(attachment.getCloudinaryPublicId(), null, ObjectUtils.asMap(
                        "resource_type", "raw", "type", attachment.getDeliveryType(), "secure", true,
                        "expires_at", clock.instant().plusSeconds(300).getEpochSecond()));
            } catch(Exception ex) { throw new IllegalStateException("Không thể tạo liên kết tải tệp.",ex); }
        }
        return cloudinary.url().secure(true).signed(true).resourceType(attachment.getResourceType())
                .type(attachment.getDeliveryType()).generate(attachment.getCloudinaryPublicId());
    }
}
