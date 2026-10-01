package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final TaiKhoanRepository accountRepository;
    private final RoleBasedAuthenticationSuccessHandler roleHandler;
    private final HttpSessionSecurityContextRepository contextRepository = new HttpSessionSecurityContextRepository();

    public GoogleOAuth2SuccessHandler(TaiKhoanRepository accountRepository, RoleBasedAuthenticationSuccessHandler roleHandler) {
        this.accountRepository = accountRepository;
        this.roleHandler = roleHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        Boolean verified = oauthUser.getAttribute("email_verified");
        if (email == null || Boolean.FALSE.equals(verified)) {
            response.sendRedirect(request.getContextPath() + "/login?msg=Google%20không%20xác%20minh%20được%20email");
            return;
        }
        TaiKhoan account = accountRepository.findByEmail(email.trim().toLowerCase()).orElse(null);
        if (account == null || !"HoatDong".equalsIgnoreCase(account.getTrangThai())) {
            response.sendRedirect(request.getContextPath() + "/login?msg=Tài%20khoản%20Google%20chưa%20được%20đăng%20ký%20hoặc%20chưa%20kích%20hoạt");
            return;
        }
        CustomUserDetails principal = new CustomUserDetails(account);
        UsernamePasswordAuthenticationToken localAuthentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(localAuthentication);
        SecurityContextHolder.setContext(context);
        contextRepository.saveContext(context, request, response);
        roleHandler.onAuthenticationSuccess(request, response, localAuthentication);
    }
}
