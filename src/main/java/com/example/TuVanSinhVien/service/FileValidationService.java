package com.example.TuVanSinhVien.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileValidationService {

    private static final Logger log = LoggerFactory.getLogger(FileValidationService.class);

    public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L; // 10 MiB
    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "application/pdf"
    );

    public static class FileValidationResult {
        private final boolean valid;
        private final String message;
        private final String safeFileName;

        public FileValidationResult(boolean valid, String message, String safeFileName) {
            this.valid = valid;
            this.message = message;
            this.safeFileName = safeFileName;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getSafeFileName() { return safeFileName; }
    }

    public FileValidationResult validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return new FileValidationResult(false, "Tệp tải lên không được để trống.", null);
        }

        // 1. Kiểm tra kích thước <= 10MB
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("[UPLOAD-SECURITY] Tệp vượt quá kích thước 10MB: {} bytes", file.getSize());
            return new FileValidationResult(false, "Kích thước tệp vượt quá giới hạn cho phép (tối đa 10 MB).", null);
        }

        // 2. Làm sạch tên tệp chống Path Traversal (loại bỏ ../, ..\, null bytes)
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "upload_file";
        }
        String sanitizedName = Paths.get(originalName).getFileName().toString().replaceAll("[^a-zA-Z0-9._-]", "_");

        // 3. Kiểm tra định dạng phần mở rộng
        String lowerName = sanitizedName.toLowerCase();
        if (!lowerName.endsWith(".jpg") && !lowerName.endsWith(".jpeg") && !lowerName.endsWith(".png") && !lowerName.endsWith(".pdf")) {
            return new FileValidationResult(false, "Định dạng tệp không được hỗ trợ. Chỉ chấp nhận JPG, PNG, PDF.", null);
        }

        // 4. Kiểm tra Content-Type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            log.warn("[UPLOAD-SECURITY] Content-Type không hợp lệ: {}", contentType);
            return new FileValidationResult(false, "Loại tệp không hợp lệ theo MIME type.", null);
        }

        // 5. Kiểm tra Magic Bytes (File Signatures)
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);
            if (read < 4) {
                return new FileValidationResult(false, "Tệp bị hỏng hoặc không đúng định dạng.", null);
            }

            if (!isSignatureValid(header, lowerName)) {
                log.warn("[UPLOAD-SECURITY] Magic bytes không khớp với phần mở rộng tệp: {}", sanitizedName);
                return new FileValidationResult(false, "Nội dung tệp không khớp với chữ ký định dạng thực tế (Magic bytes mismatch).", null);
            }
        } catch (IOException e) {
            log.error("[UPLOAD-SECURITY] Lỗi đọc nội dung tệp: {}", e.getMessage());
            return new FileValidationResult(false, "Không thể đọc nội dung tệp để kiểm tra an toàn.", null);
        }

        // 6. Sinh tên tệp an toàn duy nhất trên đĩa
        String extension = "";
        int dotIdx = sanitizedName.lastIndexOf('.');
        if (dotIdx >= 0) {
            extension = sanitizedName.substring(dotIdx);
        }
        String safeUniqueName = UUID.randomUUID().toString() + extension;

        return new FileValidationResult(true, "Tệp hợp lệ.", safeUniqueName);
    }

    private boolean isSignatureValid(byte[] header, String lowerName) {
        // JPEG: FF D8 FF
        if ((lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg"))
                && (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if (lowerName.endsWith(".png")
                && (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47) {
            return true;
        }
        // PDF: %PDF (25 50 44 46)
        if (lowerName.endsWith(".pdf")
                && (header[0] & 0xFF) == 0x25 && (header[1] & 0xFF) == 0x50 && (header[2] & 0xFF) == 0x44 && (header[3] & 0xFF) == 0x46) {
            return true;
        }
        return false;
    }
}
