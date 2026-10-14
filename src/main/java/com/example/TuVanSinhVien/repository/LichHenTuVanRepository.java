package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.LichHenTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichHenTuVanRepository extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<LichHenTuVan>, JpaRepository<LichHenTuVan, Integer> {

    List<LichHenTuVan> findBySinhVien_MaSVOrderByNgayTaoDesc(Integer maSV);

    List<LichHenTuVan> findByNguoiTuVan_MaCVOrderByNgayTaoDesc(Integer maCV);

    List<LichHenTuVan> findByNguoiTuVan_MaCVAndTrangThaiOrderByNgayTaoAsc(Integer maCV, String trangThai);

    @Query("SELECT COUNT(l) FROM LichHenTuVan l WHERE l.sinhVien.maSV = :maSV AND l.trangThai = 'ChoDuyet'")
    long countPendingByStudent(@Param("maSV") Integer maSV);
    @Query("SELECT COUNT(l) FROM LichHenTuVan l WHERE l.sinhVien.maSV=:maSV AND l.trangThai='ChoDuyet' AND l.hanDuyet>:now AND l.khungGio.batDau>:now")
    long countCurrentPendingByStudent(@Param("maSV") Integer maSV, @Param("now") LocalDateTime now);
    @Query("SELECT l FROM LichHenTuVan l WHERE l.trangThai='ChoDuyet' AND (l.hanDuyet<=:now OR l.khungGio.batDau<=:now)")
    List<LichHenTuVan> findExpiredPending(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(l) > 0 FROM LichHenTuVan l WHERE l.khungGio.maKhungGio = :maKhungGio AND l.trangThai IN ('ChoDuyet', 'DaXacNhan', 'HoanThanh')")
    boolean isSlotOccupied(@Param("maKhungGio") Integer maKhungGio);

    @Query("SELECT COUNT(l) > 0 FROM LichHenTuVan l WHERE l.sinhVien.maSV = :maSV AND l.trangThai IN ('ChoDuyet', 'DaXacNhan') AND l.khungGio.batDau < :end AND l.khungGio.ketThuc > :start")
    boolean existsOverlappingStudentAppointment(@Param("maSV") Integer maSV, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByTrangThai(String trangThai);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from LichHenTuVan e where e."+"maLichHen = :id")
    java.util.Optional<LichHenTuVan> findLockedById(@Param("id") Integer id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    List<LichHenTuVan> findByTrangThaiAndHanDuyetLessThanEqual(String status, LocalDateTime time);
    @Query("SELECT COUNT(l)>0 FROM LichHenTuVan l WHERE l.nguoiTuVan.maCV=:maCV AND l.khungGio.batDau<:end AND l.khungGio.ketThuc>:start "
            + "AND (l.trangThai IN ('DaXacNhan','HoanThanh') OR (l.trangThai='ChoDuyet' AND l.hanDuyet>:now AND l.khungGio.batDau>:now))")
    boolean existsReservedAdvisorAppointment(@Param("maCV") Integer maCV, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(l)>0 FROM LichHenTuVan l WHERE l.sinhVien.maSV=:maSV AND l.khungGio.batDau<:end AND l.khungGio.ketThuc>:start "
            + "AND (l.trangThai IN ('DaXacNhan','HoanThanh') OR (l.trangThai='ChoDuyet' AND l.hanDuyet>:now AND l.khungGio.batDau>:now))")
    boolean existsReservedStudentAppointment(@Param("maSV") Integer maSV, @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end, @Param("now") LocalDateTime now);
}
