package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.CauHinhHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CauHinhHeThongRepository extends JpaRepository<CauHinhHeThong, String> {
    Optional<CauHinhHeThong> findByKhoa(String khoa);
}
