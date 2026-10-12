package com.example.TuVanSinhVien.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;

@Service
public class CaptchaService {

    public static final String SESSION_CAPTCHA_KEY = "QAUTE_CAPTCHA_ANSWER";
    private static final String CHARACTERS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"; // loại bỏ ký tự dễ nhầm lẫn như 0, O, 1, I
    private final SecureRandom random = new SecureRandom();

    public byte[] generateCaptchaImage(HttpSession session) throws IOException {
        int width = 140;
        int height = 45;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // 1. Nền sáng với gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(245, 247, 250), width, height, new Color(225, 232, 240));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, width, height);

        // 2. Vẽ các đường kẻ ngẫu nhiên chống OCR
        g2d.setStroke(new BasicStroke(1.2f));
        for (int i = 0; i < 6; i++) {
            g2d.setColor(new Color(150 + random.nextInt(50), 160 + random.nextInt(50), 180 + random.nextInt(50)));
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g2d.drawLine(x1, y1, x2, y2);
        }

        // 3. Sinh chuỗi ngẫu nhiên 5 ký tự
        StringBuilder captchaCode = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            captchaCode.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        String solution = captchaCode.toString();
        session.setAttribute(SESSION_CAPTCHA_KEY, solution);

        // 4. Vẽ từng ký tự với góc xoay nhẹ
        Font font = new Font("Arial", Font.BOLD, 26);
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();

        Color[] textColors = {
                new Color(30, 58, 138),  // dark blue
                new Color(15, 118, 110), // teal
                new Color(126, 34, 206), // purple
                new Color(180, 83, 9)    // amber dark
        };

        int startX = 18;
        for (int i = 0; i < solution.length(); i++) {
            char c = solution.charAt(i);
            g2d.setColor(textColors[random.nextInt(textColors.length)]);

            // Góc xoay từ -15 đến +15 độ
            double angle = (random.nextDouble() - 0.5) * 0.5;
            int y = 32 + random.nextInt(4) - 2;

            g2d.rotate(angle, startX + i * 22, y);
            g2d.drawString(String.valueOf(c), startX + i * 22, y);
            g2d.rotate(-angle, startX + i * 22, y);
        }

        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    public boolean validateCaptcha(HttpSession session, String answer) {
        if (session == null || answer == null || answer.isBlank()) {
            return false;
        }
        String expected = (String) session.getAttribute(SESSION_CAPTCHA_KEY);
        // Sau khi kiểm tra, luôn xóa mã khỏi session để chống replay attack
        session.removeAttribute(SESSION_CAPTCHA_KEY);

        if (expected == null) {
            return false;
        }
        return expected.equalsIgnoreCase(answer.trim());
    }
}
