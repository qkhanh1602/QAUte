package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {
    @org.springframework.data.jpa.repository.Query("SELECT t FROM ThongBao t WHERE t.taiKhoanNhan.maTK=:maTK AND (t.lienKet IS NULL OR t.lienKet<>'/advisor/slots') ORDER BY t.ngayTao DESC")
    List<ThongBao> findByTaiKhoanNhan_MaTKOrderByNgayTaoDesc(Integer maTK);
    List<ThongBao> findByTaiKhoanNhan_MaTKAndDaDocFalseOrderByNgayTaoDesc(Integer maTK);
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) FROM ThongBao t WHERE t.taiKhoanNhan.maTK=:maTK AND t.daDoc=false AND (t.lienKet IS NULL OR t.lienKet<>'/advisor/slots')")
    long countByTaiKhoanNhan_MaTKAndDaDocFalse(Integer maTK);
    boolean existsByTaiKhoanNhan_MaTKAndKhoaChongLap(Integer maTK, String khoaChongLap);
    Optional<ThongBao> findByMaThongBaoAndTaiKhoanNhan_MaTK(Long maThongBao, Integer maTK);
}
