package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmail(String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
    List<TaiKhoan> findByVaiTro(String vaiTro);
    long countByVaiTro(String vaiTro);
}
