package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.entity.NguoiTuVan;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import com.example.TuVanSinhVien.repository.NguoiTuVanRepository;
import com.example.TuVanSinhVien.repository.SinhVienRepository;
import com.example.TuVanSinhVien.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final TaiKhoanRepository taiKhoanRepository;
    private final SinhVienRepository sinhVienRepository;
    @Autowired(required = false)
    private NguoiTuVanRepository advisorRepository;

    @Autowired
    public CustomUserDetailsService(final TaiKhoanRepository taiKhoanRepository,
                                    final SinhVienRepository sinhVienRepository,
                                    final NguoiTuVanRepository advisorRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.sinhVienRepository = sinhVienRepository;
        this.advisorRepository = advisorRepository;
    }

    public CustomUserDetailsService(final TaiKhoanRepository taiKhoanRepository,
                                    final SinhVienRepository sinhVienRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.sinhVienRepository = sinhVienRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .or(() -> taiKhoanRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản: " + username));
        boolean alsoStudent = "CoVan".equals(tk.getVaiTro()) && sinhVienRepository.findByTaiKhoan_MaTK(tk.getMaTK()).isPresent();
        boolean alsoAdvisor = "SinhVien".equals(tk.getVaiTro()) && advisorRepository != null
                && advisorRepository.findByTaiKhoan_MaTK(tk.getMaTK()).filter(a -> Boolean.TRUE.equals(a.getNhanTuVan())).isPresent();
        return new CustomUserDetails(tk, alsoStudent, alsoAdvisor);
    }
}
