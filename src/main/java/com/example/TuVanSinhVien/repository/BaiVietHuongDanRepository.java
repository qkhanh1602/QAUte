package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.BaiVietHuongDan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaiVietHuongDanRepository extends JpaRepository<BaiVietHuongDan, Integer> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "linhVuc")
    List<BaiVietHuongDan> findByTrangThaiOrderByNgayDangDesc(String trangThai);

    List<BaiVietHuongDan> findByTrangThaiAndLinhVuc_MaLinhVucOrderByNgayDangDesc(String trangThai, Integer maLinhVuc);

    @Query("SELECT b FROM BaiVietHuongDan b WHERE b.trangThai = 'XuatBan' AND (LOWER(b.tieuDe) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tomTat) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tuKhoa) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<BaiVietHuongDan> searchPublicArticles(@Param("kw") String keyword);

    List<BaiVietHuongDan> findByTrangThai(String trangThai);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "linhVuc")
    @Query("SELECT b FROM BaiVietHuongDan b WHERE b.trangThai='XuatBan' AND (:fieldId IS NULL OR b.linhVuc.maLinhVuc=:fieldId) AND (LOWER(b.tieuDe) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tomTat) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tuKhoa) LIKE LOWER(CONCAT('%', :kw, '%'))) ORDER BY b.ngayDang DESC, b.maBaiViet DESC")
    List<BaiVietHuongDan> searchPublic(@Param("fieldId") Integer fieldId, @Param("kw") String keyword);

    @org.springframework.data.jpa.repository.Modifying(flushAutomatically=true, clearAutomatically=true)
    @Query("UPDATE BaiVietHuongDan e SET e.luotXem=e.luotXem+1 WHERE e.maBaiViet=:id AND e.trangThai='XuatBan'")
    int incrementPublicView(@Param("id") Integer id);

    java.util.Optional<BaiVietHuongDan> findByMaBaiVietAndTrangThai(Integer id, String status);
}
