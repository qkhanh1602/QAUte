package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {
    List<ThongBao> findByTaiKhoanNhan_MaTKOrderByNgayTaoDesc(Integer maTK);
    List<ThongBao> findByTaiKhoanNhan_MaTKAndDaDocFalseOrderByNgayTaoDesc(Integer maTK);
    long countByTaiKhoanNhan_MaTKAndDaDocFalse(Integer maTK);
    boolean existsByTaiKhoanNhan_MaTKAndKhoaChongLap(Integer maTK, String khoaChongLap);
}
