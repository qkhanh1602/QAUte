package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.DanhGiaBuoiTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DanhGiaBuoiTuVanRepository extends JpaRepository<DanhGiaBuoiTuVan, Integer> {
    Optional<DanhGiaBuoiTuVan> findByLichHen_MaLichHen(Integer maLichHen);
    boolean existsByLichHen_MaLichHen(Integer maLichHen);
}
