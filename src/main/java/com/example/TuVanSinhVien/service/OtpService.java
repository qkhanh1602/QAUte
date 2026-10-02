package com.example.TuVanSinhVien.service;

import com.example.TuVanSinhVien.entity.YeuCauXacMinhOTP;
import com.example.TuVanSinhVien.repository.YeuCauXacMinhOTPRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);

    public static final int OTP_EXPIRY_MINUTES = 10;
    public static final int MAX_VERIFY_ATTEMPTS = 5;
    public static final int RESEND_COOLDOWN_SECONDS = 60;
    public static final int MAX_SENDS_PER_HOUR = 3;

    private final YeuCauXacMinhOTPRepository otpRepo;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public OtpService(YeuCauXacMinhOTPRepository otpRepo,
                      EmailService emailService,
                      PasswordEncoder passwordEncoder,
                      SecurityAuditService auditService) {
        this.otpRepo = otpRepo;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public static class OtpSendResult {
        private final boolean success;
        private final String message;
        private final long cooldownRemainingSeconds;

        public OtpSendResult(boolean success, String message, long cooldownRemainingSeconds) {
            this.success = success;
            this.message = message;
            this.cooldownRemainingSeconds = cooldownRemainingSeconds;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public long getCooldownRemainingSeconds() { return cooldownRemainingSeconds; }
    }

    public static class OtpVerificationResult {
        private final boolean success;
        private final String message;
        private final int remainingAttempts;

        public OtpVerificationResult(boolean success, String message, int remainingAttempts) {
            this.success = success;
            this.message = message;
            this.remainingAttempts = remainingAttempts;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public int getRemainingAttempts() { return remainingAttempts; }
    }

    @Transactional
    public OtpSendResult generateAndSendOtp(String email, String mucDich, String clientIp, String userAgent) {
        if (email == null || email.isBlank()) {
            return new OtpSendResult(false, "Địa chỉ email không được để trống.", 0);
        }
        String cleanEmail = email.trim().toLowerCase();
        LocalDateTime now = LocalDateTime.now();
        byte[] ipHash = SecurityAuditService.hashSha256(clientIp);

        // 1. Kiểm tra giới hạn 3 lần/giờ theo email
        LocalDateTime oneHourAgo = now.minusHours(1);
        long emailSendsInHour = otpRepo.countByEmailAndNgayTaoAfter(cleanEmail, oneHourAgo);
        if (emailSendsInHour >= MAX_SENDS_PER_HOUR) {
            log.warn("[OTP-RATELIMIT] Email {} đã vượt quá {} lần gửi OTP trong 1 giờ.", cleanEmail, MAX_SENDS_PER_HOUR);
            auditService.logEvent(null, "GUI_OTP_THAT_BAI", false, clientIp, userAgent,
                    "Vượt quá giới hạn " + MAX_SENDS_PER_HOUR + " lần/giờ cho email " + cleanEmail);
            return new OtpSendResult(false, "Bạn đã yêu cầu gửi mã OTP quá số lần cho phép (tối đa "
                    + MAX_SENDS_PER_HOUR + " lần/giờ). Vui lòng thử lại sau 1 giờ.", 0);
        }

        // 2. Kiểm tra giới hạn 3 lần/giờ theo IPHash
        if (ipHash != null) {
            long ipSendsInHour = otpRepo.countByIpHashAndNgayTaoAfter(ipHash, oneHourAgo);
            if (ipSendsInHour >= MAX_SENDS_PER_HOUR) {
                log.warn("[OTP-RATELIMIT] IP hash {} đã vượt quá {} lần gửi OTP trong 1 giờ.", clientIp, MAX_SENDS_PER_HOUR);
                auditService.logEvent(null, "GUI_OTP_THAT_BAI", false, clientIp, userAgent,
                        "Vượt quá giới hạn " + MAX_SENDS_PER_HOUR + " lần/giờ cho IP " + clientIp);
                return new OtpSendResult(false, "Địa chỉ mạng của bạn đã yêu cầu quá nhiều mã OTP trong 1 giờ. Vui lòng thử lại sau.", 0);
            }
        }

        // 3. Kiểm tra cooldown 60 giây từ lần gửi gần nhất
        Optional<YeuCauXacMinhOTP> latestOpt = otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc(cleanEmail, mucDich);
        if (latestOpt.isPresent()) {
            YeuCauXacMinhOTP latest = latestOpt.get();
            if (latest.getGuiLaiSau() != null && latest.getGuiLaiSau().isAfter(now)) {
                long remainingSec = Duration.between(now, latest.getGuiLaiSau()).getSeconds();
                if (remainingSec < 0) remainingSec = 0;
                return new OtpSendResult(false, "Vui lòng chờ " + remainingSec + " giây trước khi yêu cầu gửi lại mã OTP.", remainingSec);
            }
        }

        // 4. Sinh OTP 6 chữ số ngẫu nhiên không thể đoán trước
        int rawOtpNumber = 100000 + secureRandom.nextInt(900000);
        String rawOtp = String.valueOf(rawOtpNumber);

        // 5. Băm OTP bằng BCrypt (không bao giờ lưu mã rõ)
        String otpHash = passwordEncoder.encode(rawOtp);

        // 6. Lưu vào bảng YeuCauXacMinhOTP
        YeuCauXacMinhOTP otpEntity = YeuCauXacMinhOTP.builder()
                .email(cleanEmail)
                .mucDich(mucDich)
                .otpHash(otpHash)
                .ngayTao(now)
                .hetHan(now.plusMinutes(OTP_EXPIRY_MINUTES))
                .soLanNhapSai(0)
                .soLanGui(latestOpt.map(o -> o.getSoLanGui() + 1).orElse(1))
                .guiLaiSau(now.plusSeconds(RESEND_COOLDOWN_SECONDS))
                .ipHash(ipHash)
                .build();

        otpRepo.save(otpEntity);

        // 7. Gửi OTP qua Email (sử dụng tài khoản Gmail SMTP qaute.edu@gmail.com)
        if (!emailService.sendOtpEmail(cleanEmail, rawOtp, OTP_EXPIRY_MINUTES)) {
            otpEntity.setDaDungLuc(now);
            otpRepo.save(otpEntity);
            return new OtpSendResult(false, "Không gửi được email xác minh. Vui lòng thử lại sau hoặc liên hệ quản trị viên.", RESEND_COOLDOWN_SECONDS);
        }
        String returnMessage = "Mã OTP đã được gửi đến email " + cleanEmail + ". Mã có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút.";

        // 8. Ghi log bảo mật
        auditService.logEvent(null, "GUI_OTP_" + mucDich.toUpperCase(), true, clientIp, userAgent,
                "Đã gửi mã OTP qua email đến: " + cleanEmail);

        return new OtpSendResult(true, returnMessage, RESEND_COOLDOWN_SECONDS);
    }

    @Transactional
    public OtpVerificationResult verifyOtp(String email, String mucDich, String rawOtp, String clientIp, String userAgent) {
        if (email == null || email.isBlank() || rawOtp == null || rawOtp.isBlank()) {
            return new OtpVerificationResult(false, "Vui lòng nhập đầy đủ email và mã OTP.", MAX_VERIFY_ATTEMPTS);
        }
        String cleanEmail = email.trim().toLowerCase();
        String cleanOtp = rawOtp.trim();
        LocalDateTime now = LocalDateTime.now();

        Optional<YeuCauXacMinhOTP> otpOpt = otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc(cleanEmail, mucDich);
        if (otpOpt.isEmpty()) {
            auditService.logEvent(null, "XAC_MINH_OTP_THAT_BAI", false, clientIp, userAgent,
                    "Không tìm thấy yêu cầu OTP cho email: " + cleanEmail);
            return new OtpVerificationResult(false, "Không tìm thấy yêu cầu xác thực OTP hoặc mã không tồn tại.", 0);
        }

        YeuCauXacMinhOTP otpRecord = otpOpt.get();

        // 1. Kiểm tra đã sử dụng
        if (otpRecord.getDaDungLuc() != null) {
            return new OtpVerificationResult(false, "Mã OTP này đã được sử dụng trước đó. Vui lòng yêu cầu mã mới.", 0);
        }

        // 2. Kiểm tra hết hạn (10 phút)
        if (otpRecord.getHetHan().isBefore(now)) {
            auditService.logEvent(null, "XAC_MINH_OTP_THAT_BAI", false, clientIp, userAgent,
                    "OTP đã hết hạn cho email: " + cleanEmail);
            return new OtpVerificationResult(false, "Mã OTP đã hết hiệu lực (quá 10 phút). Vui lòng bấm gửi lại mã mới.", 0);
        }

        // 3. Kiểm tra số lần nhập sai >= 5
        if (otpRecord.getSoLanNhapSai() >= MAX_VERIFY_ATTEMPTS) {
            auditService.logEvent(null, "XAC_MINH_OTP_VO_HIEU_HOA", false, clientIp, userAgent,
                    "Mã OTP đã bị vô hiệu hóa do nhập sai quá 5 lần: " + cleanEmail);
            return new OtpVerificationResult(false, "Mã OTP này đã bị vô hiệu hóa do bạn đã nhập sai quá "
                    + MAX_VERIFY_ATTEMPTS + " lần. Vui lòng yêu cầu mã OTP mới.", 0);
        }

        // 4. So khớp băm BCrypt
        boolean matches = passwordEncoder.matches(cleanOtp, otpRecord.getOtpHash());
        if (!matches) {
            int newFailedCount = otpRecord.getSoLanNhapSai() + 1;
            otpRecord.setSoLanNhapSai(newFailedCount);
            otpRepo.save(otpRecord);

            int remaining = MAX_VERIFY_ATTEMPTS - newFailedCount;
            auditService.logEvent(null, "XAC_MINH_OTP_THAT_BAI", false, clientIp, userAgent,
                    "Nhập sai OTP lần " + newFailedCount + " cho email " + cleanEmail);

            if (remaining <= 0) {
                return new OtpVerificationResult(false, "Bạn đã nhập sai quá " + MAX_VERIFY_ATTEMPTS
                        + " lần. Mã OTP đã bị hủy. Vui lòng bấm gửi lại mã mới.", 0);
            } else {
                return new OtpVerificationResult(false, "Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.", remaining);
            }
        }

        // 5. Xác thực thành công: Đánh dấu thời gian đã dùng
        otpRecord.setDaDungLuc(now);
        otpRepo.save(otpRecord);

        auditService.logEvent(null, "XAC_MINH_OTP_THANH_CONG", true, clientIp, userAgent,
                "Xác minh OTP thành công cho email " + cleanEmail);

        return new OtpVerificationResult(true, "Xác minh OTP thành công!", MAX_VERIFY_ATTEMPTS);
    }
}
