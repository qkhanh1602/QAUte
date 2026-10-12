package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.LinhVucTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinhVucTuVanRepository extends JpaRepository<LinhVucTuVan, Integer> {
    Optional<LinhVucTuVan> findByMaLinhVucCode(String maLinhVucCode);
    List<LinhVucTuVan> findByTrangThai(String trangThai);
    List<LinhVucTuVan> findByDonVi_MaDonVi(Integer maDonVi);
    List<LinhVucTuVan> findByDonVi_MaDonViAndTrangThai(Integer maDonVi, String trangThai);
}
