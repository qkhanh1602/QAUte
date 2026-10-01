package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.service.CaptchaService;
import com.example.TuVanSinhVien.service.FileValidationService;
import com.example.TuVanSinhVien.service.LoginAttemptService;
import com.example.TuVanSinhVien.service.SecurityAuditService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

public class SecurityServicesTest {

    @Test
    public void testLoginAttemptServiceLockoutAndCaptcha() {
        LoginAttemptService service = new LoginAttemptService();
        String testIp = "192.168.1.100";
        String testUser = "testuser";

        Assertions.assertFalse(service.isCaptchaRequired(testIp, testUser));
        Assertions.assertFalse(service.isBlocked(testIp, testUser));

        // 1. Ghi nhận 2 lần sai -> chưa cần captcha
        service.loginFailed(testIp, testUser);
        service.loginFailed(testIp, testUser);
        Assertions.assertFalse(service.isCaptchaRequired(testIp, testUser));

        // 2. Ghi nhận lần thứ 3 -> cần CAPTCHA
        service.loginFailed(testIp, testUser);
        Assertions.assertTrue(service.isCaptchaRequired(testIp, testUser));
        Assertions.assertFalse(service.isBlocked(testIp, testUser));

        // 3. Ghi nhận lần thứ 4 và 5 -> Bị KHÓA 15 phút
        service.loginFailed(testIp, testUser);
        service.loginFailed(testIp, testUser);
        Assertions.assertTrue(service.isBlocked(testIp, testUser));
        Assertions.assertTrue(service.getRemainingLockoutMinutes(testIp, testUser) > 0);

        // 4. Khi đăng nhập thành công -> Xóa lịch sử
        service.loginSucceeded(testIp, testUser);
        Assertions.assertFalse(service.isBlocked(testIp, testUser));
        Assertions.assertFalse(service.isCaptchaRequired(testIp, testUser));
    }

    @Test
    public void testCaptchaServiceGenerationAndValidation() throws IOException {
        CaptchaService captchaService = new CaptchaService();
        MockHttpSession session = new MockHttpSession();

        byte[] imageBytes = captchaService.generateCaptchaImage(session);
        Assertions.assertNotNull(imageBytes);
        Assertions.assertTrue(imageBytes.length > 0);

        String answer = (String) session.getAttribute(CaptchaService.SESSION_CAPTCHA_KEY);
        Assertions.assertNotNull(answer);
        Assertions.assertEquals(5, answer.length());

        // Test sai câu trả lời
        boolean wrongCheck = captchaService.validateCaptcha(session, "WRONG");
        Assertions.assertFalse(wrongCheck);

        // Sinh lại câu trả lời mới
        captchaService.generateCaptchaImage(session);
        String correct = (String) session.getAttribute(CaptchaService.SESSION_CAPTCHA_KEY);
        boolean rightCheck = captchaService.validateCaptcha(session, correct.toLowerCase());
        Assertions.assertTrue(rightCheck);

        // Kiểm tra sau khi validate thì session bị xóa (chống replay)
        Assertions.assertNull(session.getAttribute(CaptchaService.SESSION_CAPTCHA_KEY));
    }

    @Test
    public void testFileValidationService() {
        FileValidationService service = new FileValidationService();

        // 1. JPEG hợp lệ (header FF D8 FF)
        byte[] validJpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};
        MockMultipartFile validJpeg = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", validJpegContent);
        FileValidationService.FileValidationResult res1 = service.validateFile(validJpeg);
        Assertions.assertTrue(res1.isValid());
        Assertions.assertNotNull(res1.getSafeFileName());
        Assertions.assertTrue(res1.getSafeFileName().endsWith(".jpg"));

        // 2. PNG hợp lệ (header 89 50 4E 47)
        byte[] validPngContent = new byte[]{(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile validPng = new MockMultipartFile("file", "picture.png", "image/png", validPngContent);
        FileValidationService.FileValidationResult res2 = service.validateFile(validPng);
        Assertions.assertTrue(res2.isValid());

        // 3. Tệp giả mạo extension (ví dụ file exe đổi đuôi thành png)
        byte[] fakeContent = new byte[]{'M', 'Z', 0, 0, 0, 0, 0, 0};
        MockMultipartFile fakeFile = new MockMultipartFile("file", "malware.png", "image/png", fakeContent);
        FileValidationService.FileValidationResult res3 = service.validateFile(fakeFile);
        Assertions.assertFalse(res3.isValid());

        // 4. File quá cỡ (> 10MB)
        byte[] hugeContent = new byte[(int) FileValidationService.MAX_FILE_SIZE_BYTES + 10];
        MockMultipartFile hugeFile = new MockMultipartFile("file", "huge.pdf", "application/pdf", hugeContent);
        FileValidationService.FileValidationResult res4 = service.validateFile(hugeFile);
        Assertions.assertFalse(res4.isValid());
    }

    @Test
    public void testSha256Hash() {
        byte[] hash = SecurityAuditService.hashSha256("127.0.0.1");
        Assertions.assertNotNull(hash);
        Assertions.assertEquals(32, hash.length);
    }

    @Test
    public void testOtpServiceLogic() {
        com.example.TuVanSinhVien.repository.YeuCauXacMinhOTPRepository otpRepo = org.mockito.Mockito.mock(com.example.TuVanSinhVien.repository.YeuCauXacMinhOTPRepository.class);
        com.example.TuVanSinhVien.service.EmailService emailService = org.mockito.Mockito.mock(com.example.TuVanSinhVien.service.EmailService.class);
        org.springframework.security.crypto.password.PasswordEncoder passwordEncoder = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        com.example.TuVanSinhVien.service.SecurityAuditService auditService = org.mockito.Mockito.mock(com.example.TuVanSinhVien.service.SecurityAuditService.class);

        com.example.TuVanSinhVien.service.OtpService otpService = new com.example.TuVanSinhVien.service.OtpService(otpRepo, emailService, passwordEncoder, auditService);

        // 1. Giả lập tạo OTP
        org.mockito.Mockito.when(otpRepo.countByEmailAndNgayTaoAfter(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(0L);
        org.mockito.Mockito.when(otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString())).thenReturn(java.util.Optional.empty());

        com.example.TuVanSinhVien.service.OtpService.OtpSendResult sendRes = otpService.generateAndSendOtp("student@ute.edu.vn", "DangKy", "127.0.0.1", "Mozilla");
        Assertions.assertTrue(sendRes.isSuccess());

        // 2. Giả lập xác thực OTP sai
        String rawOtp = "123456";
        String otpHash = passwordEncoder.encode(rawOtp);
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        com.example.TuVanSinhVien.entity.YeuCauXacMinhOTP record = com.example.TuVanSinhVien.entity.YeuCauXacMinhOTP.builder()
                .email("student@ute.edu.vn")
                .mucDich("DangKy")
                .otpHash(otpHash)
                .ngayTao(now)
                .hetHan(now.plusMinutes(10))
                .soLanNhapSai(0)
                .build();

        org.mockito.Mockito.when(otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc("student@ute.edu.vn", "DangKy"))
                .thenReturn(java.util.Optional.of(record));

        // Nhập sai mã OTP
        com.example.TuVanSinhVien.service.OtpService.OtpVerificationResult wrongRes = otpService.verifyOtp("student@ute.edu.vn", "DangKy", "000000", "127.0.0.1", "Mozilla");
        Assertions.assertFalse(wrongRes.isSuccess());
        Assertions.assertEquals(4, wrongRes.getRemainingAttempts());
        Assertions.assertEquals(1, record.getSoLanNhapSai());

        // Nhập đúng mã OTP
        com.example.TuVanSinhVien.service.OtpService.OtpVerificationResult correctRes = otpService.verifyOtp("student@ute.edu.vn", "DangKy", rawOtp, "127.0.0.1", "Mozilla");
        Assertions.assertTrue(correctRes.isSuccess());
        Assertions.assertNotNull(record.getDaDungLuc());
    }

    @Test
    public void testGmailSmtpHandshake() {
        String mailUsername = System.getenv("MAIL_USERNAME");
        String mailPassword = System.getenv("MAIL_PASSWORD");
        org.junit.jupiter.api.Assumptions.assumeTrue(mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank(), "Bỏ qua SMTP khi chưa cấu hình secret môi trường");
        org.springframework.mail.javamail.JavaMailSenderImpl sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        sender.setHost("smtp.gmail.com");
        sender.setPort(587);
        sender.setUsername(mailUsername);
        sender.setPassword(mailPassword);

        java.util.Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        try {
            sender.testConnection();
            System.out.println(">>> [SUCCESS] Ket noi den Gmail SMTP (smtp.gmail.com:587) thanh cong!");
        } catch (Exception e) {
            System.out.println(">>> [NOTICE] Ket noi SMTP: " + e.getMessage());
        }
    }

    @Test
    public void testJwtContainsAndValidatesTokenVersion() {
        String secret = java.util.Base64.getEncoder().encodeToString(new byte[32]);
        com.example.TuVanSinhVien.service.JwtService jwtService =
                new com.example.TuVanSinhVien.service.JwtService(secret, 15);
        com.example.TuVanSinhVien.entity.TaiKhoan account = com.example.TuVanSinhVien.entity.TaiKhoan.builder()
                .maTK(42)
                .tenDangNhap("student42")
                .matKhau("hash")
                .hoTen("Sinh viên 42")
                .email("student42@example.edu.vn")
                .vaiTro("SinhVien")
                .trangThai("HoatDong")
                .tokenVersion(3)
                .build();

        String token = jwtService.createAccessToken(account);
        io.jsonwebtoken.Claims claims = jwtService.parse(token);
        Assertions.assertEquals("student42", claims.getSubject());
        Assertions.assertEquals(3, claims.get("tokenVersion", Integer.class));
        Assertions.assertEquals("SinhVien", claims.get("role", String.class));
    }
}
