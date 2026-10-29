package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.repository.SinhVienRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdvisorRoleModelAdvice {
    private final SinhVienRepository sinhVienRepo;

    public AdvisorRoleModelAdvice(SinhVienRepository sinhVienRepo) {
        this.sinhVienRepo = sinhVienRepo;
    }

    @ModelAttribute("isStudentAdvisor")
    public boolean isStudentAdvisor(@AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return false;
        }
        boolean hasAdvisorRole = user.getAuthorities().stream().anyMatch(a -> "ROLE_CoVan".equals(a.getAuthority()));
        return hasAdvisorRole && sinhVienRepo.findByTaiKhoan_MaTK(user.getMaTK()).isPresent();
    }
}
