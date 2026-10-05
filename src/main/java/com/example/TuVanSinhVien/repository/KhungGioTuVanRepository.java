package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.KhungGioTuVan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KhungGioTuVanRepository extends JpaRepository<KhungGioTuVan, Integer> {

    List<KhungGioTuVan> findByNguoiTuVan_MaCVOrderByBatDauAsc(Integer maCV);

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.trangThai = 'Mo' AND k.nguoiTuVan.nhanTuVan = true AND k.nguoiTuVan.taiKhoan.trangThai = 'HoatDong' AND k.nguoiTuVan.taiKhoan.vaiTro = 'CoVan' AND k.nguoiTuVan.donVi.hoatDong = true AND k.batDau >= :fromTime AND k.batDau <= :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAvailableSlotsWithinWindow(@Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.trangThai = 'Mo' AND k.nguoiTuVan.nhanTuVan = true AND k.nguoiTuVan.taiKhoan.trangThai = 'HoatDong' AND k.nguoiTuVan.taiKhoan.vaiTro = 'CoVan' AND k.nguoiTuVan.donVi.hoatDong = true AND k.batDau >= :fromTime AND k.batDau <= :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAvailableSlotsByAdvisor(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT COUNT(k) > 0 FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.trangThai = 'Mo' AND k.nguoiTuVan.nhanTuVan = true AND k.nguoiTuVan.taiKhoan.trangThai = 'HoatDong' AND k.nguoiTuVan.taiKhoan.vaiTro = 'CoVan' AND k.nguoiTuVan.donVi.hoatDong = true AND k.batDau < :end AND k.ketThuc > :start")
    boolean existsOverlappingSlot(@Param("maCV") Integer maCV, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.batDau >= :fromTime AND k.batDau < :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAdvisorSlotsInRange(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT COUNT(k) FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.trangThai = 'Mo' AND k.nguoiTuVan.nhanTuVan = true AND k.nguoiTuVan.taiKhoan.trangThai = 'HoatDong' AND k.nguoiTuVan.taiKhoan.vaiTro = 'CoVan' AND k.nguoiTuVan.donVi.hoatDong = true AND k.batDau >= :fromTime AND k.batDau < :toTime")
    long countOpenSlotsInRange(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from KhungGioTuVan e where e."+"maKhungGio = :id")
    java.util.Optional<KhungGioTuVan> findLockedById(@Param("id") Integer id);
    @Query("select count(k) > 0 from KhungGioTuVan k where k.nguoiTuVan.maCV=:advisorId and k.maKhungGio<>:slotId and k.trangThai='Mo' and k.batDau<:end and k.ketThuc>:start")
    boolean overlapsOther(@Param("advisorId") Integer advisorId, @Param("slotId") Integer slotId,
                          @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
