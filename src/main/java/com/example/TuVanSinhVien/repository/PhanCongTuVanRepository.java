package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.PhanCongTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhanCongTuVanRepository extends JpaRepository<PhanCongTuVan, Integer> {
    List<PhanCongTuVan> findByYeuCauTuVan_MaYeuCauOrderByNgayPhanCongDesc(Integer maYeuCau);
    Optional<PhanCongTuVan> findByYeuCauTuVan_MaYeuCauAndNgayKetThucIsNull(Integer maYeuCau);
}
