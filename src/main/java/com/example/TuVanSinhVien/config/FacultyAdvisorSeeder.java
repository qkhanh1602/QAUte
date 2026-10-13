package com.example.TuVanSinhVien.config;

import com.example.TuVanSinhVien.entity.*;
import com.example.TuVanSinhVien.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Locale;

/** Demo data only: never enable the dev profile on a public deployment. */
@Component
@Profile("dev")
@Order(1)
public class FacultyAdvisorSeeder implements CommandLineRunner {
    private final DonViRepository units;
    private final TaiKhoanRepository accounts;
    private final NguoiTuVanRepository advisors;
    private final LinhVucTuVanRepository fields;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public FacultyAdvisorSeeder(DonViRepository units, TaiKhoanRepository accounts,
            NguoiTuVanRepository advisors, LinhVucTuVanRepository fields,
            PasswordEncoder encoder, Clock clock) {
        this.units = units;
        this.accounts = accounts;
        this.advisors = advisors;
        this.fields = fields;
        this.encoder = encoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (DonVi unit : units.findByHoatDongTrue()) {
            if (!"Khoa".equals(unit.getLoaiDonVi())) continue;
            boolean covered = advisors.findByDonVi_MaDonVi(unit.getMaDonVi()).stream()
                    .anyMatch(a -> Boolean.TRUE.equals(a.getNhanTuVan())
                            && "CoVan".equals(a.getTaiKhoan().getVaiTro())
                            && "HoatDong".equals(a.getTaiKhoan().getTrangThai()));
            String code = unit.getMaDonViCode().toLowerCase(Locale.ROOT);
            String username = "covan." + code;
            String staffCode = "DEMO_" + unit.getMaDonViCode();
            if (covered) {
                advisors.findByMaCanBo(staffCode).filter(a -> a.getDonVi().getMaDonVi().equals(unit.getMaDonVi()))
                        .ifPresent(a -> {
                            ensureDemoFields(unit);
                        });
                continue;
            }
            // Do not reset, unlock or repurpose an existing account.
            if (accounts.existsByTenDangNhap(username) || advisors.existsByMaCanBo(staffCode)
                    || accounts.existsByEmail(username + "@example.com")) continue;
            LocalDateTime now = LocalDateTime.now(clock);
            TaiKhoan account = accounts.save(TaiKhoan.builder()
                    .tenDangNhap(username).matKhau(encoder.encode("Covan@123"))
                    .hoTen(advisorName(unit.getMaDonViCode()))
                    .email(username + "@example.com").vaiTro("CoVan").trangThai("HoatDong")
                    .batBuocDoiMatKhau(false).ngayTao(now).ngayCapNhat(now).build());
            ensureDemoFields(unit);
            advisors.save(NguoiTuVan.builder().maCanBo(staffCode)
                    .chucVu("Cố vấn học tập")
                    .chuyenMon("Tư vấn học tập, chuyên ngành, đồ án và định hướng nghề nghiệp")
                    .donVi(unit).taiKhoan(account).nhanTuVan(true).hanNgachTiepNhan(20)
                    .build());
        }
    }

    // Fictional display names for demo accounts; never replace a manually edited name.
    private String advisorName(String code) {
        return switch (code.toUpperCase(Locale.ROOT)) {
            case "CKCTM" -> "ThS. Nguyễn Đức Thành";
            case "CNHHTP" -> "ThS. Phạm Thị Ngọc Hà";
            case "CTLUAT" -> "ThS. Trần Quốc Bảo";
            case "DDT" -> "ThS. Lê Minh Tuấn";
            case "DTTT" -> "ThS. Nguyễn Thị Bảo Trân";
            case "GTNL" -> "ThS. Võ Hoàng Phúc";
            case "INTT" -> "ThS. Đặng Thị Thu Hương";
            case "KHUD" -> "ThS. Bùi Quang Huy";
            case "KINHTE" -> "ThS. Nguyễn Thị Thanh Thảo";
            case "NGOAINGU" -> "ThS. Trần Thị Mai Anh";
            case "TTDL" -> "ThS. Phan Ngọc Diễm";
            case "VSPKT" -> "ThS. Lê Quốc Khánh";
            case "XAYDUNG" -> "ThS. Nguyễn Hoàng Nam";
            case "CNTT" -> "ThS. Phạm Minh Đức";
            default -> "ThS. Nguyễn Minh An";
        };
    }

    private void ensureDemoFields(DonVi unit) {
        var active = fields.findByDonVi_MaDonViAndTrangThai(unit.getMaDonVi(), "HoatDong");
        if (!active.isEmpty()) return;
        String fieldCode = "DEMO_" + unit.getMaDonViCode();
        // A suspended field is not automatically reactivated.
        if (fields.findByMaLinhVucCode(fieldCode).isPresent()) return;
        fields.save(LinhVucTuVan.builder()
                .maLinhVucCode(fieldCode).tenLinhVuc("Học tập và định hướng chuyên ngành - " + unit.getMaDonViCode())
                .moTa("Lĩnh vực mẫu để kiểm thử tư vấn theo khoa")
                .donVi(unit).trangThai("HoatDong").nhayCam(false).build());
    }
}
