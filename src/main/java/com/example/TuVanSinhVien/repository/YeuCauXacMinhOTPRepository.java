package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.YeuCauXacMinhOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface YeuCauXacMinhOTPRepository extends JpaRepository<YeuCauXacMinhOTP, UUID> {

    Optional<YeuCauXacMinhOTP> findTopByEmailAndMucDichOrderByNgayTaoDesc(String email, String mucDich);

    long countByEmailAndNgayTaoAfter(String email, LocalDateTime after);

    long countByIpHashAndNgayTaoAfter(byte[] ipHash, LocalDateTime after);
}
