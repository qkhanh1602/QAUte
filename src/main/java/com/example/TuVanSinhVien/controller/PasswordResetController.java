package com.example.TuVanSinhVien.controller;
import com.example.TuVanSinhVien.service.PasswordResetService;
import com.example.TuVanSinhVien.config.CustomAuthenticationFailureHandler;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {
    private final PasswordResetService service;
    public PasswordResetController(PasswordResetService service) {this.service=service;}
    @GetMapping("/forgot-password") public String page() { return "auth/forgot-password"; }
    @PostMapping("/forgot-password")
    public String submit(@RequestParam String email,@RequestParam(defaultValue="request") String action,
            @RequestParam(defaultValue="") String otp,@RequestParam(defaultValue="") String password,
            @RequestParam(defaultValue="") String confirm,HttpServletRequest request,HttpSession session,RedirectAttributes flash) {
        try {
            String ip=CustomAuthenticationFailureHandler.getClientIp(request), agent=request.getHeader("User-Agent");
            if("reset".equals(action)) {
                var result=service.reset(email,otp,password,confirm,ip,agent);
                if(!result.isSuccess()) throw new IllegalArgumentException(result.getMessage());
                session.invalidate(); flash.addFlashAttribute("successMessage","Đã đặt lại mật khẩu. Vui lòng đăng nhập bằng mật khẩu mới.");
                return "redirect:/login";
            }
            if(!"request".equals(action)) throw new IllegalArgumentException("Thao tác không hợp lệ.");
            service.request(email,ip,agent);
            flash.addFlashAttribute("infoMessage","Nếu email thuộc tài khoản đang hoạt động và còn hạn mức gửi, mã xác minh sẽ được gửi tới hộp thư. Mã có hiệu lực 10 phút; gửi lại cách nhau ít nhất 60 giây.");
        } catch(IllegalArgumentException ex) { flash.addFlashAttribute("errorMessage",ex.getMessage()); }
        flash.addFlashAttribute("resetEmail",email);
        return "redirect:/forgot-password";
    }
}
