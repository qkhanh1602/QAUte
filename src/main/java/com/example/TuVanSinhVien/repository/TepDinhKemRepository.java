package com.example.TuVanSinhVien.repository;

import com.example.TuVanSinhVien.entity.TepDinhKem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TepDinhKemRepository extends JpaRepository<TepDinhKem, Long> {
    List<TepDinhKem> findByYeuCauTuVan_MaYeuCauOrderByNgayTaoAsc(Integer id);
    List<TepDinhKem> findByPhanHoiTuVan_MaPhanHoiOrderByNgayTaoAsc(Integer id);
    List<TepDinhKem> findByTinNhanChat_MaTinNhanOrderByNgayTaoAsc(Long id);
    List<TepDinhKem> findByBaiVietHuongDan_MaBaiVietOrderByNgayTaoAsc(Integer id);
}
