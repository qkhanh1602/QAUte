package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.LichSuYeuCau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuYeuCauRepository extends JpaRepository<LichSuYeuCau, Long> {
    List<LichSuYeuCau> findByYeuCauTuVan_MaYeuCauOrderByNgayTaoDesc(Integer maYeuCau);
}
