package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.service.CaptchaService;
import com.example.TuVanSinhVien.service.LoginAttemptService;
import com.example.TuVanSinhVien.service.SecurityAuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CaptchaAuthenticationFilter extends OncePerRequestFilter {

    private final LoginAttemptService loginAttemptService;
    private final CaptchaService captchaService;
    private final SecurityAuditService securityAuditService;

    public CaptchaAuthenticationFilter(LoginAttemptService loginAttemptService,
                                       CaptchaService captchaService,
                                       SecurityAuditService securityAuditService) {
        this.loginAttemptService = loginAttemptService;
        this.captchaService = captchaService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Chỉ chặn khi request là POST /login
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
            String ip = CustomAuthenticationFailureHandler.getClientIp(request);
            String username = request.getParameter("username");
            String userAgent = request.getHeader("User-Agent");

            // 1. Kiểm tra tài khoản hoặc IP có đang bị khóa 15 phút không
            if (loginAttemptService.isBlocked(ip, username)) {
                long remaining = loginAttemptService.getRemainingLockoutMinutes(ip, username);
                securityAuditService.logEvent(null, "CHAN_TRUY_CAP_KHOA_15P", false, ip, userAgent,
                        "Chặn yêu cầu đăng nhập khi đang bị khóa: " + username);
                response.sendRedirect(request.getContextPath() + "/login?locked=true&minutes=" + remaining);
                return;
            }

            // 2. Kiểm tra nếu cần xác thực CAPTCHA (sai >= 3 lần)
            if (loginAttemptService.isCaptchaRequired(ip, username)) {
                String captchaInput = request.getParameter("captcha");
                HttpSession session = request.getSession(false);

                boolean captchaValid = captchaService.validateCaptcha(session, captchaInput);
                if (!captchaValid) {
                    loginAttemptService.loginFailed(ip, username);
                    securityAuditService.logEvent(null, "CAPTCHA_SAI", false, ip, userAgent,
                            "Nhập sai hoặc thiếu mã CAPTCHA cho user: " + username);

                    if (loginAttemptService.isBlocked(ip, username)) {
                        long remaining = loginAttemptService.getRemainingLockoutMinutes(ip, username);
                        response.sendRedirect(request.getContextPath() + "/login?locked=true&minutes=" + remaining);
                    } else {
                        response.sendRedirect(request.getContextPath() + "/login?error=true&captcha=true&msg="
                                + URLEncoder.encode("Mã xác nhận CAPTCHA không chính xác hoặc đã hết hạn!", StandardCharsets.UTF_8));
                    }
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
