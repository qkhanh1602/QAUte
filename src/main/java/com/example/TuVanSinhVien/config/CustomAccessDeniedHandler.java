package com.example.TuVanSinhVien.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        if (auth != null && auth.isAuthenticated()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_Admin".equals(a.getAuthority()));
            boolean isCoVan = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CoVan".equals(a.getAuthority()));
            boolean isSinhVien = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SinhVien".equals(a.getAuthority()));

            // 1. Nếu Admin truy cập các chức năng sinh viên
            if (isAdmin && uri.startsWith(contextPath + "/student")) {
                response.sendRedirect(contextPath + "/admin/dashboard?denied=student");
                return;
            }

            // 2. Nếu Cố vấn truy cập các chức năng sinh viên -> chuyển sang cổng Cố vấn tương ứng
            if (isCoVan && uri.startsWith(contextPath + "/student/chat")) {
                response.sendRedirect(contextPath + "/advisor/chat");
                return;
            }
            if (isCoVan && uri.startsWith(contextPath + "/student/appointments")) {
                response.sendRedirect(contextPath + "/advisor/appointments");
                return;
            }
            if (isCoVan && uri.startsWith(contextPath + "/student/tickets")) {
                response.sendRedirect(contextPath + "/advisor/tickets");
                return;
            }
            if (isCoVan && uri.startsWith(contextPath + "/student")) {
                response.sendRedirect(contextPath + "/advisor/dashboard?denied=student");
                return;
            }

            // 3. Nếu Sinh viên cố truy cập cổng Quản trị hoặc Cố vấn
            if (isSinhVien && (uri.startsWith(contextPath + "/admin") || uri.startsWith(contextPath + "/advisor"))) {
                response.sendRedirect(contextPath + "/student/dashboard?denied=role");
                return;
            }
        }

        // Trường hợp khác: forward tới trang 403 thân thiện
        request.setAttribute("errorMessage", accessDeniedException.getMessage());
        request.getRequestDispatcher("/error/403").forward(request, response);
    }
}
