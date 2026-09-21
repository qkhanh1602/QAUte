package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.DanhGiaYeuCau;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DanhGiaYeuCauRepository extends JpaRepository<DanhGiaYeuCau, Integer> {
    Optional<DanhGiaYeuCau> findByYeuCauTuVan_MaYeuCau(Integer maYeuCau);
    boolean existsByYeuCauTuVan_MaYeuCau(Integer maYeuCau);
}
