package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface YeuCauTuVanRepository extends JpaRepository<YeuCauTuVan, Integer> {

    List<YeuCauTuVan> findBySinhVien_MaSVOrderByNgayTaoDesc(Integer maSV);

    List<YeuCauTuVan> findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(Integer maCV);

    List<YeuCauTuVan> findByDonVi_MaDonViAndTrangThaiOrderByNgayTaoAsc(Integer maDonVi, String trangThai);

    List<YeuCauTuVan> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    long countByTrangThai(String trangThai);

    @Query("SELECT COUNT(y) FROM YeuCauTuVan y WHERE y.trangThai IN ('DaPhanHoi', 'Dong')")
    long countCompleted();

    @Query("SELECT y FROM YeuCauTuVan y WHERE y.trangThai = 'ChoTiepNhan' AND y.hanTiepNhan < :now")
    List<YeuCauTuVan> findOverdueAcceptance(@Param("now") LocalDateTime now);

    @Query("SELECT y FROM YeuCauTuVan y WHERE y.trangThai = 'DangXuLy' AND y.hanSLA < :now")
    List<YeuCauTuVan> findOverdueSla(@Param("now") LocalDateTime now);

    @Query("SELECT y.donVi.tenDonVi, COUNT(y) FROM YeuCauTuVan y GROUP BY y.donVi.tenDonVi")
    List<Object[]> countGroupByDonVi();

    @Query("SELECT y.trangThai, COUNT(y) FROM YeuCauTuVan y GROUP BY y.trangThai")
    List<Object[]> countGroupByTrangThai();
}
