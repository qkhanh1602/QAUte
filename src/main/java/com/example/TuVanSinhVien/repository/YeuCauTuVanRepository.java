package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.YeuCauTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface YeuCauTuVanRepository extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<YeuCauTuVan>, JpaRepository<YeuCauTuVan, Integer> {

    List<YeuCauTuVan> findBySinhVien_MaSVOrderByNgayTaoDesc(Integer maSV);

    List<YeuCauTuVan> findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(Integer maCV);

    List<YeuCauTuVan> findByDonVi_MaDonViAndTrangThaiOrderByNgayTaoAsc(Integer maDonVi, String trangThai);

    List<YeuCauTuVan> findByTrangThaiOrderByNgayTaoDesc(String trangThai);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    List<YeuCauTuVan> findByTrangThaiAndHanTuDongDongLessThanEqual(String trangThai, LocalDateTime deadline);

    long countByTrangThai(String trangThai);

    @Query("SELECT COUNT(y) FROM YeuCauTuVan y WHERE y.trangThai IN ('DaPhanHoi', 'Dong')")
    long countCompleted();

    @Query("SELECT y FROM YeuCauTuVan y WHERE y.trangThai = 'ChoTiepNhan' AND y.hanTiepNhan < :now")
    List<YeuCauTuVan> findOverdueAcceptance(@Param("now") LocalDateTime now);

    @Query("SELECT y FROM YeuCauTuVan y WHERE y.trangThai IN ('ChoTiepNhan', 'DangXuLy') AND y.ngayPhanHoiDau IS NULL AND y.hanSLA < :now")
    List<YeuCauTuVan> findOverdueSla(@Param("now") LocalDateTime now);

    @Query("SELECT y.donVi.tenDonVi, COUNT(y) FROM YeuCauTuVan y GROUP BY y.donVi.tenDonVi")
    List<Object[]> countGroupByDonVi();

    @Query("SELECT y.trangThai, COUNT(y) FROM YeuCauTuVan y GROUP BY y.trangThai")
    List<Object[]> countGroupByTrangThai();

    @Query("SELECT y FROM YeuCauTuVan y WHERE (:fromTime IS NULL OR y.ngayTao >= :fromTime) AND (:toTime IS NULL OR y.ngayTao < :toTime) AND (:unitId IS NULL OR y.donVi.maDonVi = :unitId) ORDER BY y.ngayTao DESC")
    List<YeuCauTuVan> findForReport(@Param("fromTime") LocalDateTime fromTime,
                                   @Param("toTime") LocalDateTime toTime,
                                   @Param("unitId") Integer unitId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from YeuCauTuVan e where e."+"maYeuCau = :id")
    java.util.Optional<YeuCauTuVan> findLockedById(@Param("id") Integer id);
}
