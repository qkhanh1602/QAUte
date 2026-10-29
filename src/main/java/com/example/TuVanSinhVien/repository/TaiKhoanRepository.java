package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
    Optional<TaiKhoan> findByEmail(String email);
    boolean existsByTenDangNhap(String tenDangNhap);
    boolean existsByEmail(String email);
    org.springframework.data.domain.Page<TaiKhoan> findByVaiTro(String role, org.springframework.data.domain.Pageable pageable);

    @Query("select t from TaiKhoan t where (:role is null or t.vaiTro = :role) and ("
            + "lower(t.hoTen) like lower(concat('%', :kw, '%')) or "
            + "lower(t.tenDangNhap) like lower(concat('%', :kw, '%')) or "
            + "lower(t.email) like lower(concat('%', :kw, '%')) or "
            + "(t.soDienThoai is not null and t.soDienThoai like concat('%', :kw, '%')))")
    org.springframework.data.domain.Page<TaiKhoan> searchUsers(@Param("role") String role, @Param("kw") String kw, org.springframework.data.domain.Pageable pageable);

    List<TaiKhoan> findByVaiTro(String vaiTro);
    long countByVaiTro(String vaiTro);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from TaiKhoan e where e.maTK = :id")
    java.util.Optional<TaiKhoan> findLockedById(@Param("id") Integer id);
}
