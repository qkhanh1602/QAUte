package com.example.TuVanSinhVien.dto;

import java.io.Serializable;

public record AdvisorRegistrationRequest(
        String tenDangNhap,
        String matKhau,
        String hoTen,
        String email,
        String soDienThoai,
        String maCanBo,
        String hocHamHocVi,
        String chucVu,
        String chuyenMon,
        Integer maDonVi
) implements Serializable {
    private static final long serialVersionUID = 1L;
}
