package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final TaiKhoan taiKhoan;
    private final boolean alsoStudent;
    private final boolean alsoAdvisor;

    public CustomUserDetails(TaiKhoan taiKhoan) {
        this(taiKhoan, false, false);
    }

    public CustomUserDetails(TaiKhoan taiKhoan, boolean alsoStudent) {
        this(taiKhoan, alsoStudent, false);
    }

    public CustomUserDetails(TaiKhoan taiKhoan, boolean alsoStudent, boolean alsoAdvisor) {
        this.taiKhoan = taiKhoan;
        this.alsoStudent = alsoStudent;
        this.alsoAdvisor = alsoAdvisor;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + taiKhoan.getVaiTro()));
        if ("CoVan".equals(taiKhoan.getVaiTro()) && alsoStudent) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SinhVien"));
        }
        if ("SinhVien".equals(taiKhoan.getVaiTro()) && alsoAdvisor) {
            authorities.add(new SimpleGrantedAuthority("ROLE_CoVan"));
        }
        return authorities;
    }

    public boolean isAlsoStudent() {
        return alsoStudent;
    }

    public boolean isAlsoAdvisor() {
        return alsoAdvisor;
    }

    @Override
    public String getPassword() {
        return taiKhoan.getMatKhau();
    }

    @Override
    public String getUsername() {
        return taiKhoan.getTenDangNhap();
    }

    public String getHoTen() {
        return taiKhoan.getHoTen();
    }

    public String getVaiTro() {
        return taiKhoan.getVaiTro();
    }

    public Integer getMaTK() {
        return taiKhoan.getMaTK();
    }

    public String getAvatarUrl() {
        return taiKhoan.getAvatarUrl();
    }

    public String getAvatarInitial() {
        return taiKhoan.getAvatarInitial();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !"Khoa".equalsIgnoreCase(taiKhoan.getTrangThai());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return "HoatDong".equalsIgnoreCase(taiKhoan.getTrangThai());
    }

    @java.lang.SuppressWarnings("all")
    @lombok.Generated
    public TaiKhoan getTaiKhoan() {
        return this.taiKhoan;
    }
}
