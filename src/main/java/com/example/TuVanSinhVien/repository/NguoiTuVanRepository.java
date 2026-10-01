package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.NguoiTuVan;
import com.example.TuVanSinhVien.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NguoiTuVanRepository extends JpaRepository<NguoiTuVan, Integer> {
    Optional<NguoiTuVan> findByMaCanBo(String maCanBo);
    boolean existsByMaCanBo(String maCanBo);
    Optional<NguoiTuVan> findByTaiKhoan(TaiKhoan taiKhoan);
    Optional<NguoiTuVan> findByTaiKhoan_MaTK(Integer maTK);
    List<NguoiTuVan> findByDonVi_MaDonVi(Integer maDonVi);
    List<NguoiTuVan> findByNhanTuVanTrue();

    @Query("select ntv from NguoiTuVan ntv join fetch ntv.taiKhoan join fetch ntv.donVi")
    List<NguoiTuVan> findAllWithAccountAndUnit();
}
