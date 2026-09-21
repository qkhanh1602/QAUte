package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.BaiVietHuongDan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BaiVietHuongDanRepository extends JpaRepository<BaiVietHuongDan, Integer> {

    List<BaiVietHuongDan> findByTrangThaiOrderByNgayDangDesc(String trangThai);

    List<BaiVietHuongDan> findByTrangThaiAndLinhVuc_MaLinhVucOrderByNgayDangDesc(String trangThai, Integer maLinhVuc);

    @Query("SELECT b FROM BaiVietHuongDan b WHERE b.trangThai = 'XuatBan' AND (LOWER(b.tieuDe) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tomTat) LIKE LOWER(CONCAT('%', :kw, '%')) OR LOWER(b.tuKhoa) LIKE LOWER(CONCAT('%', :kw, '%')))")
    List<BaiVietHuongDan> searchPublicArticles(@Param("kw") String keyword);

    List<BaiVietHuongDan> findByTrangThai(String trangThai);
}
