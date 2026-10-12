package com.example.TuVanSinhVien;

import com.example.TuVanSinhVien.controller.AuthController;
import com.example.TuVanSinhVien.repository.*;
import com.example.TuVanSinhVien.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RegistrationFormTests {
    @ParameterizedTest
    @ValueSource(strings = {"username", "email", "mssv", "password", "otp", "success"})
    void preservesNonSecretFieldsOnEveryRejectionOnly(String scenario) {
        var accounts = mock(TaiKhoanRepository.class);
        var students = mock(SinhVienRepository.class);
        var otp = mock(OtpService.class);
        var encoder = mock(PasswordEncoder.class);
        when(encoder.encode(anyString())).thenReturn("encoded-password");
        when(accounts.existsByTenDangNhap(anyString())).thenReturn(scenario.equals("username"));
        when(accounts.existsByEmail(anyString())).thenReturn(scenario.equals("email"));
        when(students.existsByMssv(anyString())).thenReturn(scenario.equals("mssv"));
        when(otp.generateAndSendOtp(anyString(), anyString(), any(), any()))
                .thenReturn(new OtpService.OtpSendResult(!scenario.equals("otp"), "OTP result", 60));
        var controller = new AuthController(accounts, students, mock(YeuCauXacMinhOTPRepository.class),
                encoder, otp, mock(CaptchaService.class), mock(LoginAttemptService.class),
                mock(SecurityAuditService.class), mock(ProfileService.class));
        var flash = new RedirectAttributesModelMap();
        var session = mock(HttpSession.class);
        String result = controller.handleRegister("student_test", scenario.equals("password") ? "weak" : "Password123",
                "Tên <test>", "test@example.com", null, "24110000", "Khoa Kinh tế", "Ngành tự nhập",
                "Lop A", "K24", mock(HttpServletRequest.class), session, flash);
        if (scenario.equals("success")) {
            assertTrue(result.startsWith("redirect:/verify-otp?"));
            assertFalse(flash.getFlashAttributes().containsKey("registrationForm"));
        } else {
            assertEquals("redirect:/register", result);
            assertEquals(Map.of("tenDangNhap", "student_test", "hoTen", "Tên <test>", "email", "test@example.com",
                    "soDienThoai", "", "mssv", "24110000", "khoa", "Khoa Kinh tế", "nganh", "Ngành tự nhập",
                    "lop", "Lop A", "khoaHoc", "K24"), flash.getFlashAttributes().get("registrationForm"));
            assertFalse(flash.getFlashAttributes().toString().contains("Password123"));
            assertFalse(flash.getFlashAttributes().toString().contains("encoded-password"));
        }
        if (!scenario.equals("otp") && !scenario.equals("success")) {
            verifyNoInteractions(encoder, otp, session);
        }
    }
}
