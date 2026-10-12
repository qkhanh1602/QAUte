package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.PhienChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhienChatRepository extends JpaRepository<PhienChat, Integer> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    Optional<PhienChat> findById(Integer id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select p from PhienChat p where p.maPhienChat=:id")
    Optional<PhienChat> findLockedById(@org.springframework.data.repository.query.Param("id") Integer id);


    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    List<PhienChat> findBySinhVien_MaSVOrderByThoiGianBatDauDesc(Integer maSV);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    List<PhienChat> findByNguoiTuVan_MaCVOrderByThoiGianBatDauDesc(Integer maCV);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    List<PhienChat> findByDonVi_MaDonViAndTrangThaiOrderByThoiGianBatDauAsc(Integer maDonVi, String trangThai);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    List<PhienChat> findByTrangThaiOrderByThoiGianBatDauAsc(String trangThai);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    Optional<PhienChat> findFirstBySinhVien_MaSVAndTrangThaiInOrderByThoiGianBatDauDesc(Integer maSV, List<String> trangThaiList);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"sinhVien.taiKhoan", "donVi", "nguoiTuVan.taiKhoan", "nguoiTuVan.donVi"})
    Optional<PhienChat> findFirstBySinhVien_MaSVAndDonVi_MaDonViAndTrangThaiInOrderByThoiGianBatDauDesc(Integer maSV, Integer maDonVi, List<String> trangThaiList);
}
