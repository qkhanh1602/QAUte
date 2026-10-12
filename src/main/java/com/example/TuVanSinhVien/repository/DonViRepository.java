package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonViRepository extends JpaRepository<DonVi, Integer> {
    Optional<DonVi> findByMaDonViCode(String maDonViCode);
    List<DonVi> findByHoatDongTrue();
}
