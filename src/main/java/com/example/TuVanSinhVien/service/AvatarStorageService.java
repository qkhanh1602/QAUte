package com.example.TuVanSinhVien.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Service
public class AvatarStorageService {
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;

    public AvatarStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public AvatarUploadResult upload(MultipartFile file, Integer accountId) {
        validate(file);
        ensureConfigured();
        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "qaute/avatars",
                    "public_id", "account_" + accountId,
                    "overwrite", true,
                    "resource_type", "image",
                    "transformation", "c_fill,g_face,h_512,w_512,q_auto,f_auto"
            ));
            return new AvatarUploadResult(String.valueOf(result.get("secure_url")), String.valueOf(result.get("public_id")));
        } catch (IOException e) {
            throw new IllegalStateException("Không thể tải ảnh lên Cloudinary. Vui lòng thử lại.", e);
        }
    }

    private void ensureConfigured() {
        if (cloudinary.config.cloudName == null || cloudinary.config.cloudName.isBlank()
                || cloudinary.config.apiKey == null || cloudinary.config.apiKey.isBlank()
                || cloudinary.config.apiSecret == null || cloudinary.config.apiSecret.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình CLOUDINARY_URL trên máy chủ.");
        }
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện.");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("Ảnh đại diện không được vượt quá 5 MB.");
        }
        if (file.getContentType() == null || !ALLOWED_TYPES.contains(file.getContentType().toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP.");
        }
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (!isJpeg(header) && !isPng(header) && !isWebp(header)) {
                throw new IllegalArgumentException("Nội dung tệp không phải ảnh JPG, PNG hoặc WEBP hợp lệ.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể kiểm tra tệp ảnh.", e);
        }
    }

    private boolean isJpeg(byte[] h) {
        return h.length >= 3 && (h[0] & 0xff) == 0xff && (h[1] & 0xff) == 0xd8 && (h[2] & 0xff) == 0xff;
    }

    private boolean isPng(byte[] h) {
        return h.length >= 8 && (h[0] & 0xff) == 0x89 && h[1] == 0x50 && h[2] == 0x4e && h[3] == 0x47;
    }

    private boolean isWebp(byte[] h) {
        return h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
    }

    public record AvatarUploadResult(String secureUrl, String publicId) {}
}
