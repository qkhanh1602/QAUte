package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.DonVi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonViRepository extends JpaRepository<DonVi, Integer> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from DonVi d where d.maDonVi = :id")
    Optional<DonVi> lockById(@org.springframework.data.repository.query.Param("id") Integer id);
    Optional<DonVi> findByMaDonViCode(String maDonViCode);
    List<DonVi> findByHoatDongTrue();
}
