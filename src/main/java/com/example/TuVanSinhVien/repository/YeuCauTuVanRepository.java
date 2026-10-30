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

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"donVi", "coVanPhuTrach.taiKhoan", "sinhVien.taiKhoan"})
    @Query(value="SELECT y FROM YeuCauTuVan y ORDER BY CASE WHEN y.trangThai='ChoTiepNhan' THEN 0 ELSE 1 END, y.ngayTao DESC, y.maYeuCau DESC",
           countQuery="SELECT COUNT(y) FROM YeuCauTuVan y")
    org.springframework.data.domain.Page<YeuCauTuVan> findAdminPage(org.springframework.data.domain.Pageable pageable);

    List<YeuCauTuVan> findBySinhVien_MaSVOrderByNgayTaoDesc(Integer maSV);

    List<YeuCauTuVan> findByCoVanPhuTrach_MaCVOrderByNgayTaoDesc(Integer maCV);

    @Query("SELECT y FROM YeuCauTuVan y WHERE y.coVanPhuTrach.maCV = :advisorId "
            + "ORDER BY CASE WHEN y.trangThai IN ('ChoTiepNhan', 'DangXuLy') THEN 0 "
            + "WHEN y.trangThai = 'DaPhanHoi' THEN 1 ELSE 2 END, y.hanSLA ASC, y.ngayTao ASC, y.maYeuCau ASC")
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "linhVuc", "donVi", "coVanPhuTrach.taiKhoan"})
    List<YeuCauTuVan> findAdvisorTicketsByDeadline(@Param("advisorId") Integer advisorId);


    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "linhVuc", "donVi", "coVanPhuTrach.taiKhoan"})
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
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @Query("SELECT new com.example.TuVanSinhVien.service.ReportRow(y.tieuDe, y.ngayTao, studentAccount.hoTen, "
            + "unit.tenDonVi, field.tenLinhVuc, y.trangThai, advisorAccount.hoTen) FROM YeuCauTuVan y "
            + "JOIN y.sinhVien student JOIN student.taiKhoan studentAccount JOIN y.donVi unit JOIN y.linhVuc field "
            + "LEFT JOIN y.coVanPhuTrach advisor LEFT JOIN advisor.taiKhoan advisorAccount "
            + "WHERE (:fromTime IS NULL OR y.ngayTao >= :fromTime) AND (:toTime IS NULL OR y.ngayTao < :toTime) "
            + "AND (:unitId IS NULL OR unit.maDonVi = :unitId) ORDER BY y.ngayTao DESC, y.maYeuCau DESC")
    List<com.example.TuVanSinhVien.service.ReportRow> findReportRows(@Param("fromTime") LocalDateTime fromTime,
                                                                  @Param("toTime") LocalDateTime toTime,
                                                                  @Param("unitId") Integer unitId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from YeuCauTuVan e where e."+"maYeuCau = :id")
    java.util.Optional<YeuCauTuVan> findLockedById(@Param("id") Integer id);
}
