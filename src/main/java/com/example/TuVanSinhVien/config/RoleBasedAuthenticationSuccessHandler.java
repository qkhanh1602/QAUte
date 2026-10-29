package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.service.LoginAttemptService;
import com.example.TuVanSinhVien.service.SecurityAuditService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService securityAuditService;

    public RoleBasedAuthenticationSuccessHandler(LoginAttemptService loginAttemptService,
                                                SecurityAuditService securityAuditService) {
        this.loginAttemptService = loginAttemptService;
        this.securityAuditService = securityAuditService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String ip = CustomAuthenticationFailureHandler.getClientIp(request);
        String username = authentication.getName();
        String userAgent = request.getHeader("User-Agent");

        // Xóa lịch sử thất bại khi đăng nhập thành công
        loginAttemptService.loginSucceeded(ip, username);

        // Ghi log bảo mật
        securityAuditService.logEvent(null, "DANG_NHAP_THANH_CONG", true, ip, userAgent,
                "Người dùng " + username + " đăng nhập thành công");

        if (authentication.getPrincipal() instanceof CustomUserDetails user) {
            if (Boolean.TRUE.equals(user.getTaiKhoan().getBatBuocDoiMatKhau())) {
                response.sendRedirect(request.getContextPath() + "/profile?passwordRequired=true");
                return;
            }
            String primaryRole = user.getVaiTro();
            if ("CoVan".equals(primaryRole)) {
                response.sendRedirect(request.getContextPath() + "/advisor/dashboard");
                return;
            } else if ("SinhVien".equals(primaryRole)) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            } else if ("Admin".equals(primaryRole)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_CoVan".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/advisor/dashboard");
                return;
            } else if ("ROLE_SinhVien".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/student/dashboard");
                return;
            } else if ("ROLE_Admin".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            }
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
