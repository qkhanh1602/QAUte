package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.CauHoiFAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CauHoiFAQRepository extends JpaRepository<CauHoiFAQ, Integer> {

    List<CauHoiFAQ> findByTrangThaiOrderByLuotXemDesc(String trangThai);

    List<CauHoiFAQ> findByTrangThaiAndLinhVuc_MaLinhVucOrderByLuotXemDesc(String trangThai, Integer maLinhVuc);

    @Query("SELECT f FROM CauHoiFAQ f WHERE f.trangThai = 'HienThi' AND (LOWER(f.cauHoi) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(f.cauTraLoi) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(f.tuKhoa) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<CauHoiFAQ> searchPublicFaq(@Param("kw") String keyword);

    List<CauHoiFAQ> findByTrangThai(String trangThai);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "linhVuc")
    @Query("SELECT f FROM CauHoiFAQ f WHERE f.trangThai='HienThi' AND (:fieldId IS NULL OR f.linhVuc.maLinhVuc=:fieldId) AND (LOWER(f.cauHoi) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(f.cauTraLoi) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(f.tuKhoa) LIKE LOWER(CONCAT('%', :kw, '%'))) ORDER BY f.luotXem DESC, f.maFAQ DESC")
    List<CauHoiFAQ> searchPublic(@Param("fieldId") Integer fieldId, @Param("kw") String keyword);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("UPDATE CauHoiFAQ e SET e.luotXem=e.luotXem+1 WHERE e.maFAQ=:id AND e.trangThai='HienThi'")
    int incrementPublicView(@Param("id") Integer id);

    java.util.Optional<CauHoiFAQ> findByMaFAQAndTrangThai(Integer id, String status);
}
