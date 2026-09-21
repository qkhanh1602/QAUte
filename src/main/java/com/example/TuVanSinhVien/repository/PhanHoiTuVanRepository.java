package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.PhanHoiTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhanHoiTuVanRepository extends JpaRepository<PhanHoiTuVan, Integer> {
    List<PhanHoiTuVan> findByYeuCauTuVan_MaYeuCauOrderByNgayTraLoiAsc(Integer maYeuCau);
}
