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

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.trangThai = 'Mo' AND k.batDau >= :fromTime AND k.batDau <= :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAvailableSlotsWithinWindow(@Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.trangThai = 'Mo' AND k.batDau >= :fromTime AND k.batDau <= :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAvailableSlotsByAdvisor(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT COUNT(k) > 0 FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.batDau < :end AND k.ketThuc > :start")
    boolean existsOverlappingSlot(@Param("maCV") Integer maCV, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT k FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.batDau >= :fromTime AND k.batDau < :toTime ORDER BY k.batDau ASC")
    List<KhungGioTuVan> findAdvisorSlotsInRange(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);

    @Query("SELECT COUNT(k) FROM KhungGioTuVan k WHERE k.nguoiTuVan.maCV = :maCV AND k.trangThai = 'Mo' AND k.batDau >= :fromTime AND k.batDau < :toTime")
    long countOpenSlotsInRange(@Param("maCV") Integer maCV, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime);
}
