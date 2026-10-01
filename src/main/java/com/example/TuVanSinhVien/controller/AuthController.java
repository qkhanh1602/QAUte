package com.example.TuVanSinhVien.controller;

import com.example.TuVanSinhVien.config.CustomAuthenticationFailureHandler;
import com.example.TuVanSinhVien.config.CustomUserDetails;
import com.example.TuVanSinhVien.dto.AdvisorRegistrationRequest;
import com.example.TuVanSinhVien.dto.RegistrationRequest;
import com.example.TuVanSinhVien.entity.DonVi;
import com.example.TuVanSinhVien.entity.NguoiTuVan;
import com.example.TuVanSinhVien.entity.SinhVien;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.DonViRepository;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
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

    public static final String SESSION_PENDING_REG = "PENDING_REGISTRATION";
    public static final String SESSION_PENDING_ADVISOR_REG = "PENDING_ADVISOR_REGISTRATION";

    private final TaiKhoanRepository taiKhoanRepo;
    private final SinhVienRepository sinhVienRepo;
    private final NguoiTuVanRepository nguoiTuVanRepo;
    private final DonViRepository donViRepo;
    private final YeuCauXacMinhOTPRepository otpRepo;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final CaptchaService captchaService;
    private final LoginAttemptService loginAttemptService;
    private final SecurityAuditService auditService;
    private final ProfileService profileService;

    public AuthController(TaiKhoanRepository taiKhoanRepo,
                          SinhVienRepository sinhVienRepo,
                          NguoiTuVanRepository nguoiTuVanRepo,
                          DonViRepository donViRepo,
                          YeuCauXacMinhOTPRepository otpRepo,
                          PasswordEncoder passwordEncoder,
                          OtpService otpService,
                          CaptchaService captchaService,
                          LoginAttemptService loginAttemptService,
                          SecurityAuditService auditService,
                          ProfileService profileService) {
        this.taiKhoanRepo = taiKhoanRepo;
        this.sinhVienRepo = sinhVienRepo;
        this.nguoiTuVanRepo = nguoiTuVanRepo;
        this.donViRepo = donViRepo;
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
                            @RequestParam(required = false) String advisorPending,
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
        if (advisorPending != null) {
            model.addAttribute("successMessage", "Đăng ký cố vấn thành công. Tài khoản đang chờ Admin xác minh và phê duyệt.");
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
        if (taiKhoanRepo.existsByTenDangNhap(tenDangNhap.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập '" + tenDangNhap.trim() + "' đã tồn tại.");
            return "redirect:/register";
        }
        if (taiKhoanRepo.existsByEmail(email.trim().toLowerCase())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Email '" + email.trim() + "' đã được đăng ký.");
            return "redirect:/register";
        }
        if (sinhVienRepo.existsByMssv(mssv.trim())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã số sinh viên (MSSV) '" + mssv.trim() + "' đã tồn tại trong hệ thống.");
            return "redirect:/register";
        }
        if (matKhau == null || !matKhau.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu phải dài 8–72 ký tự, có ít nhất một chữ và một số.");
            return "redirect:/register";
        }

        // 2. Lưu thông tin đăng ký vào session
        RegistrationRequest regDto = new RegistrationRequest(
                tenDangNhap.trim(),
                passwordEncoder.encode(matKhau),
                hoTen.trim(),
                email.trim().toLowerCase(),
                soDienThoai != null ? soDienThoai.trim() : null,
                mssv.trim().toUpperCase(),
                khoa.trim(),
                nganh.trim(),
                lop.trim(),
                khoaHoc.trim()
        );
        session.removeAttribute(SESSION_PENDING_ADVISOR_REG);
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
            return "redirect:/register";
        }

        redirectAttributes.addFlashAttribute("infoMessage", sendResult.getMessage());
        return "redirect:/verify-otp?email=" + URLEncoder.encode(regDto.getEmail(), StandardCharsets.UTF_8);
    }

    @GetMapping("/advisor-register")
    public String advisorRegisterPage(Model model) {
        model.addAttribute("donViList", donViRepo.findByHoatDongTrue());
        return "auth/advisor-register";
    }

    @PostMapping("/advisor-register")
    public String handleAdvisorRegister(@RequestParam String tenDangNhap,
                                        @RequestParam String matKhau,
                                        @RequestParam String hoTen,
                                        @RequestParam String email,
                                        @RequestParam(required = false) String soDienThoai,
                                        @RequestParam String maCanBo,
                                        @RequestParam(required = false) String hocHamHocVi,
                                        @RequestParam String chucVu,
                                        @RequestParam String chuyenMon,
                                        @RequestParam Integer maDonVi,
                                        HttpServletRequest request,
                                        HttpSession session,
                                        RedirectAttributes redirectAttributes) {
        String cleanUsername = tenDangNhap.trim();
        String cleanEmail = email.trim().toLowerCase();
        String cleanStaffCode = maCanBo.trim().toUpperCase();
        String cleanFullName = hoTen.trim();
        String cleanPosition = chucVu.trim();
        String cleanExpertise = chuyenMon.trim();
        String cleanPhone = soDienThoai == null ? null : soDienThoai.trim();

        if (!cleanUsername.matches("^[A-Za-z0-9._-]{4,50}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập phải có 4–50 ký tự và chỉ gồm chữ, số, dấu chấm, gạch dưới hoặc gạch ngang.");
            return "redirect:/advisor-register";
        }
        if (cleanFullName.length() < 2 || cleanFullName.length() > 100
                || cleanStaffCode.length() < 2 || cleanStaffCode.length() > 30
                || cleanPosition.isBlank() || cleanPosition.length() > 100
                || cleanExpertise.isBlank() || cleanExpertise.length() > 255) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thông tin họ tên, mã cán bộ, chức vụ hoặc chuyên môn không hợp lệ.");
            return "redirect:/advisor-register";
        }
        if (!cleanEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || cleanEmail.length() > 254) {
            redirectAttributes.addFlashAttribute("errorMessage", "Địa chỉ email không hợp lệ.");
            return "redirect:/advisor-register";
        }
        if (cleanPhone != null && !cleanPhone.isBlank() && !cleanPhone.matches("^[0-9+() .-]{8,15}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Số điện thoại không hợp lệ.");
            return "redirect:/advisor-register";
        }

        if (taiKhoanRepo.existsByTenDangNhap(cleanUsername) || taiKhoanRepo.existsByEmail(cleanEmail)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập hoặc email đã được sử dụng.");
            return "redirect:/advisor-register";
        }
        if (nguoiTuVanRepo.existsByMaCanBo(cleanStaffCode)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mã cán bộ đã tồn tại trong hệ thống.");
            return "redirect:/advisor-register";
        }
        if (matKhau == null || !matKhau.matches("^(?=.*[A-Za-z])(?=.*\\d).{8,72}$")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu phải dài 8–72 ký tự, có ít nhất một chữ và một số.");
            return "redirect:/advisor-register";
        }
        DonVi donVi = donViRepo.findById(maDonVi).filter(dv -> Boolean.TRUE.equals(dv.getHoatDong())).orElse(null);
        if (donVi == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đơn vị đăng ký không hợp lệ hoặc đã ngừng hoạt động.");
            return "redirect:/advisor-register";
        }

        AdvisorRegistrationRequest pending = new AdvisorRegistrationRequest(
                cleanUsername, passwordEncoder.encode(matKhau), cleanFullName, cleanEmail,
                cleanPhone, cleanStaffCode,
                hocHamHocVi == null ? null : hocHamHocVi.trim(), cleanPosition, cleanExpertise, maDonVi);
        session.removeAttribute(SESSION_PENDING_REG);
        session.setAttribute(SESSION_PENDING_ADVISOR_REG, pending);

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        OtpService.OtpSendResult sendResult = otpService.generateAndSendOtp(
                cleanEmail, "DangKyCoVan", clientIp, request.getHeader("User-Agent"));
        if (!sendResult.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", sendResult.getMessage());
            return "redirect:/advisor-register";
        }
        redirectAttributes.addFlashAttribute("infoMessage", sendResult.getMessage());
        return "redirect:/verify-otp?email=" + URLEncoder.encode(cleanEmail, StandardCharsets.UTF_8);
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam(required = false) String email,
                                HttpSession session,
                                Model model) {
        String targetEmail = email;
        RegistrationRequest regDto = (RegistrationRequest) session.getAttribute(SESSION_PENDING_REG);
        AdvisorRegistrationRequest advisorReg = (AdvisorRegistrationRequest) session.getAttribute(SESSION_PENDING_ADVISOR_REG);
        if (targetEmail == null || targetEmail.isBlank()) {
            if (regDto != null) {
                targetEmail = regDto.getEmail();
            } else if (advisorReg != null) {
                targetEmail = advisorReg.email();
            }
        }

        if (targetEmail == null || targetEmail.isBlank()) {
            return "redirect:/register";
        }

        model.addAttribute("email", targetEmail);
        model.addAttribute("advisorRegistration", advisorReg != null);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    @Transactional
    public String handleVerifyOtp(@RequestParam String email,
                                  @RequestParam String otp,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String cleanEmail = email.trim().toLowerCase();

        AdvisorRegistrationRequest advisorReg = (AdvisorRegistrationRequest) session.getAttribute(SESSION_PENDING_ADVISOR_REG);
        String purpose = advisorReg == null ? "DangKy" : "DangKyCoVan";

        // 1. Xác thực OTP
        OtpService.OtpVerificationResult result = otpService.verifyOtp(cleanEmail, purpose, otp, clientIp, userAgent);
        if (!result.isSuccess()) {
            redirectAttributes.addFlashAttribute("errorMessage", result.getMessage());
            redirectAttributes.addFlashAttribute("remainingAttempts", result.getRemainingAttempts());
            return "redirect:/verify-otp?email=" + URLEncoder.encode(cleanEmail, StandardCharsets.UTF_8);
        }

        // 2. Lấy thông tin đăng ký chờ từ session
        RegistrationRequest regDto = (RegistrationRequest) session.getAttribute(SESSION_PENDING_REG);
        if (advisorReg == null && (regDto == null || !cleanEmail.equalsIgnoreCase(regDto.getEmail()))) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký đã hết hạn hoặc không tìm thấy dữ liệu. Vui lòng thực hiện đăng ký lại.");
            return "redirect:/register";
        }
        if (advisorReg != null && !cleanEmail.equalsIgnoreCase(advisorReg.email())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Phiên đăng ký cố vấn đã hết hạn. Vui lòng đăng ký lại.");
            return "redirect:/advisor-register";
        }

        if (advisorReg != null) {
            if (taiKhoanRepo.existsByTenDangNhap(advisorReg.tenDangNhap())
                    || taiKhoanRepo.existsByEmail(advisorReg.email())
                    || nguoiTuVanRepo.existsByMaCanBo(advisorReg.maCanBo())) {
                session.removeAttribute(SESSION_PENDING_ADVISOR_REG);
                redirectAttributes.addFlashAttribute("errorMessage", "Tên đăng nhập, email hoặc mã cán bộ vừa được sử dụng. Vui lòng đăng ký lại.");
                return "redirect:/advisor-register";
            }
            DonVi donVi = donViRepo.findById(advisorReg.maDonVi()).orElse(null);
            if (donVi == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Đơn vị đăng ký không còn tồn tại.");
                return "redirect:/advisor-register";
            }
            LocalDateTime now = LocalDateTime.now();
            TaiKhoan advisorAccount = TaiKhoan.builder()
                    .tenDangNhap(advisorReg.tenDangNhap()).matKhau(advisorReg.matKhau())
                    .hoTen(advisorReg.hoTen()).email(advisorReg.email()).soDienThoai(advisorReg.soDienThoai())
                    .vaiTro("CoVan").trangThai("ChoDuyet").batBuocDoiMatKhau(false)
                    .ngayTao(now).ngayCapNhat(now).build();
            advisorAccount = taiKhoanRepo.save(advisorAccount);
            NguoiTuVan advisor = NguoiTuVan.builder()
                    .maCanBo(advisorReg.maCanBo()).hocHamHocVi(advisorReg.hocHamHocVi())
                    .chucVu(advisorReg.chucVu()).chuyenMon(advisorReg.chuyenMon())
                    .donVi(donVi).taiKhoan(advisorAccount).nhanTuVan(false).hanNgachTiepNhan(20)
                    .build();
            nguoiTuVanRepo.save(advisor);
            final Integer savedAdvisorAccountId = advisorAccount.getMaTK();
            otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc(cleanEmail, purpose).ifPresent(record -> {
                record.setTaiKhoan(taiKhoanRepo.findById(savedAdvisorAccountId).orElse(null));
                otpRepo.save(record);
            });
            session.removeAttribute(SESSION_PENDING_ADVISOR_REG);
            auditService.logEvent(advisorAccount, "DANG_KY_CO_VAN_CHO_DUYET", true, clientIp, userAgent,
                    "Cố vấn đã xác thực email và đang chờ Admin duyệt: " + advisorAccount.getTenDangNhap());
            return "redirect:/login?advisorPending=true";
        }

        // 3. Tạo tài khoản chính thức (Hoạt động)
        LocalDateTime now = LocalDateTime.now();
        TaiKhoan tk = TaiKhoan.builder()
                .tenDangNhap(regDto.getTenDangNhap())
                .matKhau(regDto.getMatKhau())
                .hoTen(regDto.getHoTen())
                .email(regDto.getEmail())
                .soDienThoai(regDto.getSoDienThoai())
                .vaiTro("SinhVien")
                .trangThai("HoatDong")
                .batBuocDoiMatKhau(false)
                .ngayTao(now)
                .ngayCapNhat(now)
                .build();
        tk = taiKhoanRepo.save(tk);

        SinhVien sv = SinhVien.builder()
                .mssv(regDto.getMssv())
                .khoa(regDto.getKhoa())
                .nganh(regDto.getNganh())
                .lop(regDto.getLop())
                .khoaHoc(regDto.getKhoaHoc())
                .taiKhoan(tk)
                .build();
        sinhVienRepo.save(sv);

        // 4. Cập nhật mã tài khoản vào bản ghi OTP
        final Integer savedMaTK = tk.getMaTK();
        otpRepo.findTopByEmailAndMucDichOrderByNgayTaoDesc(cleanEmail, "DangKy").ifPresent(record -> {
            record.setTaiKhoan(taiKhoanRepo.findById(savedMaTK).orElse(null));
            otpRepo.save(record);
        });

        // 5. Xóa dữ liệu tạm khỏi session
        session.removeAttribute(SESSION_PENDING_REG);

        auditService.logEvent(tk, "KICH_HOAT_TAI_KHOAN_THANH_CONG", true, clientIp, userAgent,
                "Kích hoạt tài khoản sinh viên thành công qua OTP: " + tk.getTenDangNhap());

        return "redirect:/login?registered=true";
    }

    @PostMapping("/resend-otp")
    public String handleResendOtp(@RequestParam String email,
                                  HttpServletRequest request,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        String clientIp = CustomAuthenticationFailureHandler.getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String cleanEmail = email.trim().toLowerCase();

        AdvisorRegistrationRequest advisorReg = (AdvisorRegistrationRequest) session.getAttribute(SESSION_PENDING_ADVISOR_REG);
        String purpose = advisorReg == null ? "DangKy" : "DangKyCoVan";
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
            TaiKhoan updatedAccount = profileService.updateProfile(userDetails.getMaTK(), hoTen, soDienThoai);
            userDetails.getTaiKhoan().setHoTen(updatedAccount.getHoTen());
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
}
