package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.config.CustomAuthenticationFailureHandler;
import com.example.TuVanSinhVien.service.JwtService;
import com.example.TuVanSinhVien.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LoginAttemptService loginAttemptService;
    private final com.example.TuVanSinhVien.service.TokenRevocationService tokenRevocation;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.TuVanSinhVien.service.ProfileService profiles;

    public JwtAuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                             LoginAttemptService loginAttemptService,
                             com.example.TuVanSinhVien.service.TokenRevocationService tokenRevocation) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
        this.tokenRevocation = tokenRevocation;
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(@Valid @RequestBody TokenRequest request, HttpServletRequest servletRequest) {
        String clientIp = CustomAuthenticationFailureHandler.getClientIp(servletRequest);
        if (loginAttemptService.isBlocked(clientIp, request.username())) {
            return ResponseEntity.status(429).body(Map.of("message", "Đăng nhập tạm khóa do thử sai quá nhiều lần."));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            String token = jwtService.createAccessToken(user.getTaiKhoan());
            loginAttemptService.loginSucceeded(clientIp, request.username());
            return ResponseEntity.ok(new TokenResponse(token, "Bearer", jwtService.getAccessSeconds()));
        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(clientIp, request.username());
            return ResponseEntity.status(401).body(Map.of("message", "Thông tin đăng nhập không hợp lệ."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(503).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user) {
        tokenRevocation.revokeAll(user.getMaTK());
        return ResponseEntity.ok(Map.of("message", "Đã đăng xuất và thu hồi các token của tài khoản."));
    }

    public record TokenRequest(@NotBlank String username, @NotBlank String password) {}
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@org.springframework.security.core.annotation.AuthenticationPrincipal CustomUserDetails user,
                                            @Valid @RequestBody PasswordRequest request) {
        try {
            profiles.changePassword(user.getMaTK(), request.oldPassword(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Đã đổi mật khẩu. Vui lòng đăng nhập lại."));
        } catch (IllegalArgumentException ex) { return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage())); }
    }
    public record PasswordRequest(@NotBlank String oldPassword, @NotBlank String newPassword) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
}
