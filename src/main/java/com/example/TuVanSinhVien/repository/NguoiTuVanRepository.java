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
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"taiKhoan", "donVi"})
    Optional<NguoiTuVan> findByTaiKhoan_MaTK(Integer maTK);
    List<NguoiTuVan> findByDonVi_MaDonVi(Integer maDonVi);
    List<NguoiTuVan> findByNhanTuVanTrue();

    @Query("select n from NguoiTuVan n join fetch n.taiKhoan t join fetch n.donVi d "
            + "where n.nhanTuVan=true and t.trangThai='HoatDong' and t.vaiTro='CoVan' and d.hoatDong=true "
            + "order by d.tenDonVi, t.hoTen")
    List<NguoiTuVan> findAvailableAdvisors();

    @Query("select ntv from NguoiTuVan ntv join fetch ntv.taiKhoan join fetch ntv.donVi")
    List<NguoiTuVan> findAllWithAccountAndUnit();
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select n from NguoiTuVan n where n.maCV=:id")
    Optional<NguoiTuVan> findLockedById(@org.springframework.data.repository.query.Param("id") Integer id);
}
