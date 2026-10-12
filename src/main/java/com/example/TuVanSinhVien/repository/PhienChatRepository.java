package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.PhienChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhienChatRepository extends JpaRepository<PhienChat, Integer> {

    List<PhienChat> findBySinhVien_MaSVOrderByThoiGianBatDauDesc(Integer maSV);

    List<PhienChat> findByNguoiTuVan_MaCVOrderByThoiGianBatDauDesc(Integer maCV);

    List<PhienChat> findByDonVi_MaDonViAndTrangThaiOrderByThoiGianBatDauAsc(Integer maDonVi, String trangThai);

    List<PhienChat> findByTrangThaiOrderByThoiGianBatDauAsc(String trangThai);

    Optional<PhienChat> findFirstBySinhVien_MaSVAndTrangThaiInOrderByThoiGianBatDauDesc(Integer maSV, List<String> trangThaiList);

    Optional<PhienChat> findFirstBySinhVien_MaSVAndDonVi_MaDonViAndTrangThaiInOrderByThoiGianBatDauDesc(Integer maSV, Integer maDonVi, List<String> trangThaiList);
}
