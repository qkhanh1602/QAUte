package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.NhatKyBaoMat;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.NhatKyBaoMatRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
public class SecurityAuditService {
    @org.springframework.beans.factory.annotation.Autowired
    private java.time.Clock clock = java.time.Clock.system(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);
    private final NhatKyBaoMatRepository nhatKyBaoMatRepo;

    public SecurityAuditService(NhatKyBaoMatRepository nhatKyBaoMatRepo) {
        this.nhatKyBaoMatRepo = nhatKyBaoMatRepo;
    }

    public void logEvent(TaiKhoan taiKhoan, String loaiSuKien, boolean thanhCong, String clientIp, String userAgent, String chiTiet) {
        try {
            byte[] ipHash = hashSha256(clientIp);
            byte[] uaHash = hashSha256(userAgent);

            NhatKyBaoMat entry = NhatKyBaoMat.builder()
                    .taiKhoan(taiKhoan)
                    .loaiSuKien(loaiSuKien)
                    .thanhCong(thanhCong)
                    .ipHash(ipHash)
                    .userAgentHash(uaHash)
                    .chiTiet(chiTiet != null && chiTiet.length() > 500 ? chiTiet.substring(0, 500) : chiTiet)
                    .ngayTao(LocalDateTime.now(clock))
                    .build();

            nhatKyBaoMatRepo.save(entry);
            log.info("[AUDIT] Sự kiện: {}, Thành công: {}, Chi tiết: {}", loaiSuKien, thanhCong, chiTiet);
        } catch (Exception e) {
            log.error("[AUDIT-ERROR] Lỗi ghi nhận nhật ký bảo mật: {}", e.getMessage(), e);
        }
    }

    public static byte[] hashSha256(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            log.error("Thuật toán SHA-256 không khả dụng: {}", e.getMessage());
            return null;
        }
    }
}
