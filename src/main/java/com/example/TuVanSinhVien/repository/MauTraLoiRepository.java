package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.MauTraLoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MauTraLoiRepository extends JpaRepository<MauTraLoi, Integer> {
    List<MauTraLoi> findByDonVi_MaDonViAndHoatDongTrue(Integer maDonVi);
}
