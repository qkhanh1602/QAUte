package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.service.LoginAttemptService;
import com.example.TuVanSinhVien.service.SecurityAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationFailureHandler.class);

    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService securityAuditService;

    public CustomAuthenticationFailureHandler(LoginAttemptService loginAttemptService,
                                             SecurityAuditService securityAuditService) {
        this.loginAttemptService = loginAttemptService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String ip = getClientIp(request);
        String username = request.getParameter("username");
        String userAgent = request.getHeader("User-Agent");

        loginAttemptService.loginFailed(ip, username);
        int failedCount = loginAttemptService.getFailedAttempts(ip, username);

        securityAuditService.logEvent(null, "DANG_NHAP_THAT_BAI", false, ip, userAgent,
                "Đăng nhập thất bại cho user: " + username + " (Lần " + failedCount + "). Lý do: " + exception.getMessage());

        if (loginAttemptService.isBlocked(ip, username)) {
            long remaining = loginAttemptService.getRemainingLockoutMinutes(ip, username);
            securityAuditService.logEvent(null, "KHOA_TAI_KHOAN_TAM_THOI", false, ip, userAgent,
                    "Khóa tạm 15 phút do nhập sai 5 lần cho user: " + username);
            response.sendRedirect(request.getContextPath() + "/login?locked=true&minutes=" + remaining);
            return;
        }

        if (loginAttemptService.isCaptchaRequired(ip, username)) {
            String errorMsg = exception.getMessage() != null ? exception.getMessage() : "Sai thông tin đăng nhập";
            response.sendRedirect(request.getContextPath() + "/login?error=true&captcha=true&msg="
                    + URLEncoder.encode(errorMsg, StandardCharsets.UTF_8));
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login?error=true");
    }

    public static String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
