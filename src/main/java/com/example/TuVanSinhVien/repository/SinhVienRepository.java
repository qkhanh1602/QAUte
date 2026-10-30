package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.SinhVien;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SinhVienRepository extends JpaRepository<SinhVien, Integer> {
    Optional<SinhVien> findByMssv(String mssv);
    Optional<SinhVien> findByTaiKhoan(TaiKhoan taiKhoan);
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "taiKhoan")
    Optional<SinhVien> findByTaiKhoan_MaTK(Integer maTK);
    @org.springframework.data.jpa.repository.Query("select sv from SinhVien sv join fetch sv.taiKhoan")
    java.util.List<SinhVien> findAllWithAccount();

    boolean existsByMssv(String mssv);
}
