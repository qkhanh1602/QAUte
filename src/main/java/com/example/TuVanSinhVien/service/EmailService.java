package com.example.TuVanSinhVien.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final BrevoEmailClient brevo;
    private final String provider;
    private final String from;

    public EmailService(@Autowired(required = false) JavaMailSender mailSender, BrevoEmailClient brevo,
            @Value("${app.email.provider:smtp}") String provider,
            @Value("${app.email.from:qaute.edu@gmail.com}") String from) {
        this.mailSender = mailSender;
        this.brevo = brevo;
        this.provider = provider;
        this.from = from;
        if (!"smtp".equals(provider) && !"brevo".equals(provider))
            throw new IllegalArgumentException("EMAIL_PROVIDER phải là smtp hoặc brevo.");
    }

    public boolean sendOtpEmail(String toEmail, String otpCode, int expiryMinutes) {
            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; background-color: #ffffff;\">"
                    + "<div style=\"text-align: center; margin-bottom: 20px;\">"
                    + "<h2 style=\"color: #1e3a8a; margin: 0;\">HỆ THỐNG TƯ VẤN SINH VIÊN QAUTE</h2>"
                    + "<p style=\"color: #64748b; font-size: 14px;\">Trường Đại học Công nghệ Kỹ thuật TP.HCM</p>"
                    + "</div>"
                    + "<div style=\"padding: 20px; background-color: #f8fafc; border-radius: 6px;\">"
                    + "<p style=\"font-size: 15px; color: #334155;\">Xin chào bạn,</p>"
                    + "<p style=\"font-size: 15px; color: #334155;\">Bạn đang thực hiện xác minh email trên Cổng tư vấn trực tuyến QAUTE. Dưới đây là mã OTP xác thực email của bạn:</p>"
                    + "<div style=\"text-align: center; margin: 25px 0;\">"
                    + "<span style=\"display: inline-block; font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1e3a8a; background: #e0e7ff; padding: 12px 28px; border-radius: 8px; border: 1px dashed #6366f1;\">"
                    + otpCode
                    + "</span>"
                    + "</div>"
                    + "<p style=\"font-size: 14px; color: #ef4444; font-weight: 500;\">Lưu ý: Mã xác thực có hiệu lực trong <strong>" + expiryMinutes + " phút</strong>. Tuyệt đối không chia sẻ mã này cho bất kỳ ai khác.</p>"
                    + "<p style=\"font-size: 13px; color: #94a3b8;\">Nếu bạn không yêu cầu mã xác minh này, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "<div style=\"text-align: center; margin-top: 20px; font-size: 12px; color: #94a3b8;\">"
                    + "&copy; 2026 QAUTE - Trường Đại học Công nghệ Kỹ thuật TP.HCM. All rights reserved."
                    + "</div>"
                    + "</div>";

        return sendNotificationEmail(toEmail, "[QAUTE] Mã xác thực OTP", htmlContent);
    }

    public boolean sendNotificationEmail(String toEmail, String subject, String htmlBody) {
        if (toEmail == null || toEmail.isBlank()) {
            return false;
        }
        try {
            if ("brevo".equals(provider)) return brevo.send(toEmail, subject, htmlBody);
            if (mailSender == null) return false;
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from, "QAUTE - Ban Tư Vấn Sinh Viên");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            log.warn("[EMAIL-SERVICE] Gửi email qua {} thất bại ({}).", provider, ex.getClass().getSimpleName());
            return false;
        }
    }

}
