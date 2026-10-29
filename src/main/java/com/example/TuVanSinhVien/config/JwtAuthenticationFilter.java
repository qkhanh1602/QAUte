package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TaiKhoanRepository accountRepository;
    private SinhVienRepository studentRepository;
    @Autowired(required = false)
    private NguoiTuVanRepository advisorRepository;

    public JwtAuthenticationFilter(JwtService jwtService, TaiKhoanRepository accountRepository) {
        this(jwtService, accountRepository, null);
    }

    @Autowired
    public JwtAuthenticationFilter(JwtService jwtService,
                                   TaiKhoanRepository accountRepository,
                                   SinhVienRepository studentRepository) {
        this.jwtService = jwtService;
        this.accountRepository = accountRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var existing = SecurityContextHolder.getContext().getAuthentication();
        // Reuse only within this request; every new request still checks the database.
        TaiKhoan currentAccount = null;
        if (existing != null && existing.getPrincipal() instanceof CustomUserDetails principal) {
            TaiKhoan current = accountRepository.findById(principal.getMaTK()).orElse(null);
            currentAccount = current;
            if (current == null || !"HoatDong".equals(current.getTrangThai())
                    || !java.util.Objects.equals(current.getVaiTro(), principal.getVaiTro())
                    || !java.util.Objects.equals(current.getTokenVersion(), principal.getTaiKhoan().getTokenVersion())) {
                SecurityContextHolder.clearContext();
                var session = request.getSession(false);
                if (session != null) session.invalidate();
            }
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            SecurityContextHolder.clearContext();
            currentAccount = null;
            try {
                if (!authorization.startsWith("Bearer ")) throw new SecurityException();
                Claims claims = jwtService.parse(authorization.substring(7));
                TaiKhoan account = accountRepository.findByTenDangNhap(claims.getSubject())
                        .orElseThrow(SecurityException::new);
                int tokenVersion = claims.get("tokenVersion", Integer.class);
                int currentVersion = account.getTokenVersion() == null ? 0 : account.getTokenVersion();
                if (!"HoatDong".equals(account.getTrangThai()) || tokenVersion != currentVersion
                        || !java.util.Objects.equals(claims.get("accountId", Integer.class), account.getMaTK())
                        || !java.util.Objects.equals(claims.get("role", String.class), account.getVaiTro())) {
                    throw new SecurityException();
                }
                boolean alsoStudent = "CoVan".equals(account.getVaiTro()) && studentRepository != null
                        && studentRepository.findByTaiKhoan_MaTK(account.getMaTK()).isPresent();
                boolean alsoAdvisor = "SinhVien".equals(account.getVaiTro()) && advisorRepository != null
                        && advisorRepository.findByTaiKhoan_MaTK(account.getMaTK()).filter(a -> Boolean.TRUE.equals(a.getNhanTuVan())).isPresent();
                CustomUserDetails principal = new CustomUserDetails(account, alsoStudent, alsoAdvisor);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(claims.getExpiration().toInstant());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                currentAccount = account;
            } catch (RuntimeException ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        var active = SecurityContextHolder.getContext().getAuthentication();
        if (active != null && active.getPrincipal() instanceof CustomUserDetails) {
            String path = request.getServletPath();
            boolean passwordRoute = "/profile".equals(path) && "GET".equals(request.getMethod())
                    || "/profile/change-password".equals(path) || "/api/auth/change-password".equals(path)
                    || "/logout".equals(path) || "/api/auth/logout".equals(path)
                    || path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/images/");
            if (currentAccount != null && Boolean.TRUE.equals(currentAccount.getBatBuocDoiMatKhau()) && !passwordRoute) {
                if (authorization != null || path.startsWith("/api/") || path.startsWith("/ws/")) {
                    response.setStatus(403); response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":\"PASSWORD_CHANGE_REQUIRED\",\"message\":\"Vui lòng đổi mật khẩu tạm trước khi sử dụng hệ thống.\"}");
                } else response.sendRedirect(request.getContextPath() + "/profile?passwordRequired=true");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
