package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.NhatKyBaoMat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NhatKyBaoMatRepository extends org.springframework.data.jpa.repository.JpaSpecificationExecutor<NhatKyBaoMat>, JpaRepository<NhatKyBaoMat, Long> {
    List<NhatKyBaoMat> findTop50ByOrderByNgayTaoDesc();
}
