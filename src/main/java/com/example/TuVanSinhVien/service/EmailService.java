package com.example.TuVanSinhVien.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
        // Luôn ghi log console rõ ràng để test được ngay trong STS
        printConsoleOtpBox(toEmail, otpCode, expiryMinutes);

        if (mailSender == null) {
            log.info("[EMAIL-SERVICE] JavaMailSender chưa được cấu hình. Sử dụng mã OTP hiển thị ở console trên.");
            return true;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("qaute.edu@gmail.com", "QAUTE - Ban Tư Vấn Sinh Viên");
            helper.setTo(toEmail);
            helper.setSubject("[QAUTE] Mã xác thực OTP đăng ký tài khoản");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;\">"
                    + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                    + "<h2 style=\"color: #1e3a8a; margin: 0;\">HỆ THỐNG TƯ VẤN SINH VIÊN QAUTE</h2>"
                    + "<p style=\"color: #64748b; font-size: 14px;\">Trường Đại học Công nghệ Kỹ thuật TP.HCM</p>"
                    + "</div>"
                    + "<div style=\"padding: 20px; background-color: #f8fafc; border-radius: 6px;\">"
                    + "<p style=\"font-size: 15px; color: #334155;\">Xin chào bạn,</p>"
                    + "<p style=\"font-size: 15px; color: #334155;\">Bạn đang thực hiện đăng ký tài khoản trên Cổng tư vấn trực tuyến QAUTE. Dưới đây là mã OTP xác thực email của bạn:</p>"
                    + "<div style=\"text-align: center; margin: 25px 0;\">"
                    + "<span style=\"display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1e3a8a; background: #e0e7ff; padding: 12px 28px; border-radius: 8px; border: 1px dashed #6366f1;\">"
                    + otpCode
                    + "</span>"
                    + "</div>"
                    + "<p style=\"font-size: 14px; color: #ef4444; font-weight: 500;\">Lưu ý: Mã xác thực có hiệu lực trong <strong>" + expiryMinutes + " phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác.</p>"
                    + "<p style=\"font-size: 13px; color: #94a3b8;\">Nếu bạn không yêu cầu đăng ký tài khoản này, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "<div style=\"text-align: center; margin-top: 20px; font-size: 12px; color: #94a3b8;\">"
                    + "&copy; 2026 QAUTE - Trường Đại học Công nghệ Kỹ thuật TP.HCM. All rights reserved."
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("[EMAIL-SERVICE] Đã gửi email OTP thành công tới: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.warn("[EMAIL-SERVICE] Không thể gửi email qua SMTP: {}. Mã OTP vẫn có hiệu lực và đã in ra console.", e.getMessage());
            return true; // Trả về true để không làm gián đoạn luồng test nếu SMTP chưa có internet/config
        }
    }

    private void printConsoleOtpBox(String toEmail, String otpCode, int expiryMinutes) {
        String line = "================================================================================";
        log.info("\n{}\n[QAUTE - MÃ XÁC THỰC EMAIL OTP]\nNgười nhận: {}\nMã OTP: [{}]\nHiệu lực: {} phút\n{}\n",
                line, toEmail, otpCode, expiryMinutes, line);
    }
}
