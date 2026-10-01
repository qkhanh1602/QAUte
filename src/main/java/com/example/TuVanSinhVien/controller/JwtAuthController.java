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

    public JwtAuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                             LoginAttemptService loginAttemptService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.loginAttemptService = loginAttemptService;
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

    public record TokenRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
}
