package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomAuthenticationFailureHandler;
import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.dto.RegistrationRequest;
import com.example.TuVanSinhVien.entity.SinhVien;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import com.example.TuVanSinhVien.repository.YeuCauXacMinhOTPRepository;
import com.example.TuVanSinhVien.service.CaptchaService;
import com.example.TuVanSinhVien.service.LoginAttemptService;
import com.example.TuVanSinhVien.service.OtpService;
import com.example.TuVanSinhVien.service.ProfileService;
import com.example.TuVanSinhVien.service.SecurityAuditService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Controller
public class AuthController {

    @GetMapping("/logout")
    public String logoutConfirmation() {
        return "auth/logout";
    }

    public static final String SESSION_PENDING_REG = "PENDING_REGISTRATION";

    private final TaiKhoanRepository taiKhoanRepo;
    private final SinhVienRepository sinhVienRepo;
    private final YeuCauXacMinhOTPRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final CaptchaService captchaService;
    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService auditService;
    private final ProfileService profileService;
    @org.springframework.beans.factory.annotation.Autowired
    private com.example.TuVanSinhVien.service.RegistrationCompletionService registrationCompletion;


    public AuthController(TaiKhoanRepository taiKhoanRepo,
                          SinhVienRepository sinhVienRepo,
                          YeuCauXacMinhOTPRepository otpRepo,
                          PasswordEncoder passwordEncoder,
                          OtpService otpService,
                          CaptchaService captchaService,
                          LoginAttemptService loginAttemptService,
                          SecurityAuditService auditService,
                          ProfileService profileService) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.sinhVienRepo = sinhVienRepo;
        this.otpRepo = otpRepo;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.captchaService = captchaService;
        this.loginAttemptService = loginAttemptService;
        this.auditService = auditService;
        this.profileService = profileService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String locked,
                            @RequestParam(required = false) String minutes,
                            @RequestParam(required = false) String captcha,
                            @RequestParam(required = false) String msg,
                            HttpServletRequest request,
                            Model model) {
        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);

        if (locked != null || loginAttemptService.isBlocked(clientIp, null)) {
            long remaining = loginAttemptService.getRemainingLockoutMinutes(clientIp, null);
            if (minutes != null) {
                try {
                    remaining = Math.max(remaining, Long.parseLong(minutes));
                } catch (NumberFormatException ignored) {}
            }
            if (remaining <= 0) remaining = 15;
            model.addAttribute("lockedMessage", "Tài khoản hoặc địa chỉ IP của bạn tạm thời bị khóa trong 15 phút do nhập sai thông tin đăng nhập quá 5 lần. Vui lòng thử lại sau " + remaining + " phút.");
        } else if (msg != null && !msg.isBlank()) {
            model.addAttribute("errorMessage", msg);
        } else if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất khỏi hệ thống thành công.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Xác thực email và kích hoạt tài khoản thành công! Mời bạn đăng nhập.");
        }

        boolean requireCaptcha = (captcha != null) || loginAttemptService.isCaptchaRequired(clientIp, null);
        model.addAttribute("requireCaptcha", requireCaptcha);

        return "auth/login";
    }

    @GetMapping("/captcha")
    public ResponseEntity<byte[]> getCaptcha(HttpSession session, HttpServletResponse response) throws IOException {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0);

        byte[] imageBytes = captchaService.generateCaptchaImage(session);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes);
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@RequestParam String tenDangNhap,
                                 @RequestParam String matKhau,
                                 @RequestParam String hoTen,
                                 @RequestParam String email,
                                 @RequestParam(required = false) String soDienThoai,
                                 @RequestParam String mssv,
                                 @RequestParam String khoa,
                                 @RequestParam String nganh,
                                 @RequestParam String lop,
                                 @RequestParam String khoaHoc,
                                 HttpServletRequest request,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // 1. Kiểm tra trùng lặp cơ bản
        // Only non-secret fields are flashed back after a rejected submission.
        var form = new java.util.LinkedHashMap<String, String>();
        form.put("tenDangNhap", tenDangNhap);
        form.put("hoTen", hoTen);
        form.put("email", email);
        form.put("soDienThoai", soDienThoai == null ? "" : soDienThoai);
        form.put("mssv", mssv);
        form.put("khoa", khoa);
        form.put("nganh", nganh);
        form.put("lop", lop);
        form.put("khoaHoc", khoaHoc);
        try {
            com.example.TuVanSinhVien.service.AccountInput.identity(tenDangNhap, hoTen, email, soDienThoai);
            com.example.TuVanSinhVien.service.AccountInput.student(mssv, khoa, nganh, lop, khoaHoc);
            com.example.TuVanSinhVien.service.AccountInput.password(matKhau);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }
        if (taiKhoanRepo.existsByTenDangNhap(tenDangNhap.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập '" + tenDangNhap.trim() + "' đã tồn tại.");
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }
        if (taiKhoanRepo.existsByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + email.trim() + "' đã được đăng ký.");
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }
        if (sinhVienRepo.existsByMssv(mssv.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã số sinh viên (MSSV) '" + mssv.trim() + "' đã tồn tại trong hệ thống.");
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }
        if (matKhau == null || !matKhau.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu phải dài 8–72 ký tự, có ít nhất một chữ và một số.");
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }

        // 2. Lưu thông tin đăng ký vào session
        RegistrationRequest regDto = new RegistrationRequest(
                tenDangNhap.trim(),
                passwordEncoder.encode(matKhau),
                hoTen.trim(),
                email.trim().toLowerCase(java.util.Locale.ROOT),
                soDienThoai != null ? soDienThoai.trim() : null,
                mssv.trim().toUpperCase(),
                khoa.trim(),
                nganh.trim(),
                lop.trim(),
                khoaHoc.trim()
        );
        session.setAttribute(SESSION_PENDING_REG, regDto);

        // 3. Sinh mã OTP, băm BCrypt lưu DB và gửi qua Email
        OtpService.OtpSendResult sendResult = otpService.generateAndSendOtp(
                regDto.getEmail(),
                "DangKy",
                clientIp,
                userAgent
        );
        if (!sendResult.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", sendResult.getMessage());
            redirectAttributes.addFlashAttribute("registrationForm", form);
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("infoMessage", sendResult.getMessage());
        return "redirect:/verify-otp?email=" + URLEncoder.encode(regDto.getEmail(), StandardCharsets.UTF_8);
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email,
                                HttpSession session,
                                Model model) {
        String targetEmail = email;
        RegistrationRequest regDto = (RegistrationRequest) session.getAttribute(SESSION_PENDING_REG);
        if (targetEmail == null || targetEmail.isBlank()) {
            if (regDto != null) {
                targetEmail = regDto.getEmail();
            }
        }

        if (targetEmail == null || targetEmail.isBlank()) {
            return "redirect:/register";
        }

        model.addAttribute("email", targetEmail);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String handleVerifyOtp(@RequestParam String email,
                                  @RequestParam String otp,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String cleanEmail = email.trim().toLowerCase(java.util.Locale.ROOT);

        RegistrationRequest regDto = (RegistrationRequest) session.getAttribute(SESSION_PENDING_REG);
        if (regDto == null || !cleanEmail.equalsIgnoreCase(regDto.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        try {
            var result = registrationCompletion.complete(regDto, otp, clientIp, userAgent);
            if (!result.isSuccess()) {
                redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
                redirectAttributes.addFlashAttribute("remainingAttempts", result.getRemainingAttempts());
                return "redirect:/verify-otp?email=" + URLEncoder.encode(cleanEmail, StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException | org.springframework.dao.DataIntegrityViolationException ex) {
            var form = new java.util.LinkedHashMap<String, String>();
            form.put("tenDangNhap", regDto.getTenDangNhap()); form.put("hoTen", regDto.getHoTen());
            form.put("email", regDto.getEmail()); form.put("soDienThoai", regDto.getSoDienThoai());
            form.put("mssv", regDto.getMssv()); form.put("khoa", regDto.getKhoa()); form.put("nganh", regDto.getNganh());
            form.put("lop", regDto.getLop()); form.put("khoaHoc", regDto.getKhoaHoc());
            redirectAttributes.addFlashAttribute("registrationForm", form);
            redirectAttributes.addFlashAttribute("errorMessage", ex instanceof IllegalArgumentException ? ex.getMessage()
                    : "Tên đăng nhập, email hoặc MSSV đã được sử dụng. Vui lòng kiểm tra lại.");
            session.removeAttribute(SESSION_PENDING_REG);
            return "redirect:/register";
        }
        session.removeAttribute(SESSION_PENDING_REG);
        return "redirect:/login?registered=true";
    }

    @PostMapping("/resend-otp")
    public String handleResendOtp(@RequestParam String email,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String cleanEmail = email.trim().toLowerCase(java.util.Locale.ROOT);

        RegistrationRequest pending = (RegistrationRequest) session.getAttribute(SESSION_PENDING_REG);
        if (pending == null || !cleanEmail.equalsIgnoreCase(pending.getEmail())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/register";
        }
        String purpose = "DangKy";
        OtpService.OtpSendResult sendResult = otpService.generateAndSendOtp(cleanEmail, purpose, clientIp, userAgent);
        if (sendResult.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", sendResult.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", sendResult.getMessage());
        }

        return "redirect:/verify-otp?email=" + URLEncoder.encode(cleanEmail, StandardCharsets.UTF_8);
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        TaiKhoan tk = taiKhoanRepo.findById(userDetails.getMaTK()).orElse(null);
        model.addAttribute("user", tk);
        if (tk != null && "SinhVien".equals(tk.getVaiTro())) {
            model.addAttribute("sinhVien", sinhVienRepo.findByTaiKhoan_MaTK(tk.getMaTK()).orElse(null));
        }
        return "auth/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam String hoTen,
                                @RequestParam(required = false) String soDienThoai,
                                RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        try {
            TaiKhoan updatedAccount = profileService.updateProfile(userDetails.getMaTK(), taiKhoanRepo.findById(userDetails.getMaTK()).orElseThrow().getHoTen(), soDienThoai);

            userDetails.getTaiKhoan().setSoDienThoai(updatedAccount.getSoDienThoai());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin liên hệ thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping(value = "/profile/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateAvatar(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam("avatar") MultipartFile avatar,
                               RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        try {
            TaiKhoan updatedAccount = profileService.updateAvatar(userDetails.getMaTK(), avatar);
            userDetails.getTaiKhoan().setAvatarUrl(updatedAccount.getAvatarUrl());
            userDetails.getTaiKhoan().setAvatarPublicId(updatedAccount.getAvatarPublicId());
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật ảnh đại diện thành công!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";
        TaiKhoan tk = taiKhoanRepo.findById(userDetails.getMaTK()).orElse(null);
        if (tk != null) {
            String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
            String userAgent = request.getHeader("User-Agent");

            try {
                profileService.changePassword(userDetails.getMaTK(), oldPassword, newPassword);
                auditService.logEvent(tk, "DOI_MAT_KHAU_THANH_CONG", true, clientIp, userAgent, "Người dùng đã đổi mật khẩu thành công");
                redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công. Phiên token cũ đã bị thu hồi!");
            } catch (IllegalArgumentException e) {
                auditService.logEvent(tk, "DOI_MAT_KHAU_THAT_BAI", false, clientIp, userAgent, e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            }
        }
        return "redirect:/profile";
    }
    @PostMapping("/profile/email/request")
    public String requestEmailChange(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam String email, @RequestParam String currentPassword,
                                    HttpServletRequest request, RedirectAttributes flash) {
        if (user == null) return "redirect:/login";
        try {
            String cleanEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
            profileService.validateEmailChange(user.getMaTK(), cleanEmail, currentPassword);
            OtpService.OtpSendResult result = otpService.generateAndSendOtp(cleanEmail, "DoiEmail",
                    CustomAuthenticationFailureHandler.getClientIp(request), request.getHeader("User-Agent"));
            if (!result.isSuccess()) throw new IllegalArgumentException(result.getMessage());
            request.getSession().setAttribute("pendingProfileEmail", cleanEmail);
            request.getSession().setAttribute("pendingProfileAccount", user.getMaTK());
            flash.addFlashAttribute("successMessage", result.getMessage());
        } catch (RuntimeException ex) { flash.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:/profile";
    }

    @PostMapping("/profile/email/verify")
    public String verifyEmailChange(@AuthenticationPrincipal CustomUserDetails user, @RequestParam String otp,
                                   HttpServletRequest request, RedirectAttributes flash) {
        if (user == null) return "redirect:/login";
        try {
            Object pending = request.getSession().getAttribute("pendingProfileEmail");
            if (!(pending instanceof String email) || !user.getMaTK().equals(request.getSession().getAttribute("pendingProfileAccount")))
                throw new IllegalArgumentException("Vui lòng yêu cầu mã xác minh email trước.");
            OtpService.OtpVerificationResult result = otpService.verifyOtp(email, "DoiEmail", otp,
                    CustomAuthenticationFailureHandler.getClientIp(request), request.getHeader("User-Agent"));
            if (!result.isSuccess()) throw new IllegalArgumentException(result.getMessage());
            TaiKhoan updated = profileService.changeVerifiedEmail(user.getMaTK(), email);
            user.getTaiKhoan().setEmail(updated.getEmail());
            request.getSession().removeAttribute("pendingProfileEmail");
            request.getSession().removeAttribute("pendingProfileAccount");
            flash.addFlashAttribute("successMessage", "Đã xác minh và cập nhật email.");
        } catch (RuntimeException ex) { flash.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:/profile";
    }
}
