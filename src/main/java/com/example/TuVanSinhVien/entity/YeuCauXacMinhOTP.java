package com.example.TuVanSinhVien.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "YeuCauXacMinhOTP")
public class YeuCauXacMinhOTP {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "MaOTP")
    private UUID maOTP;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTK")
    private TaiKhoan taiKhoan;

    @Column(name = "Email", nullable = false, length = 254)
    private String email;

    @Column(name = "SoDienThoai", length = 15)
    private String soDienThoai;

    @Column(name = "KenhXacMinh", length = 10)
    private String kenhXacMinh; // EMAIL hoặc SMS

    @Column(name = "MucDich", nullable = false, length = 30)
    private String mucDich; // DangKy, DangKyCoVan, XacMinhEmail, DatLaiMatKhau, DoiEmail

    @Column(name = "OTPHash", nullable = false, length = 255)
    private String otpHash;

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    @Column(name = "HetHan", nullable = false)
    private LocalDateTime hetHan;

    @Column(name = "DaDungLuc")
    private LocalDateTime daDungLuc;

    @Column(name = "SoLanNhapSai", nullable = false, columnDefinition = "tinyint")
    private Integer soLanNhapSai;

    @Column(name = "SoLanGui", nullable = false, columnDefinition = "tinyint")
    private Integer soLanGui;

    @Column(name = "GuiLaiSau", nullable = false)
    private LocalDateTime guiLaiSau;

    @Column(name = "IPHash", columnDefinition = "binary(32)")
    private byte[] ipHash;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
        if (soLanNhapSai == null) soLanNhapSai = 0;
        if (soLanGui == null) soLanGui = 1;
        if (hetHan == null) hetHan = ngayTao.plusMinutes(10);
        if (guiLaiSau == null) guiLaiSau = ngayTao.plusSeconds(60);
        if (kenhXacMinh == null) kenhXacMinh = "EMAIL";
    }

    public YeuCauXacMinhOTP() {
    }

    public YeuCauXacMinhOTP(UUID maOTP, TaiKhoan taiKhoan, String email, String soDienThoai, String kenhXacMinh,
                           String mucDich, String otpHash, LocalDateTime ngayTao, LocalDateTime hetHan,
                           LocalDateTime daDungLuc, Integer soLanNhapSai, Integer soLanGui,
                           LocalDateTime guiLaiSau, byte[] ipHash) {
        this.maOTP = maOTP;
        this.taiKhoan = taiKhoan;
        this.email = email;
        this.soDienThoai = soDienThoai;
        this.kenhXacMinh = kenhXacMinh;
        this.mucDich = mucDich;
        this.otpHash = otpHash;
        this.ngayTao = ngayTao;
        this.hetHan = hetHan;
        this.daDungLuc = daDungLuc;
        this.soLanNhapSai = soLanNhapSai;
        this.soLanGui = soLanGui;
        this.guiLaiSau = guiLaiSau;
        this.ipHash = ipHash;
    }

    public UUID getMaOTP() {
        return maOTP;
    }

    public void setMaOTP(UUID maOTP) {
        this.maOTP = maOTP;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getKenhXacMinh() {
        return kenhXacMinh;
    }

    public void setKenhXacMinh(String kenhXacMinh) {
        this.kenhXacMinh = kenhXacMinh;
    }

    public String getMucDich() {
        return mucDich;
    }

    public void setMucDich(String mucDich) {
        this.mucDich = mucDich;
    }

    public String getOtpHash() {
        return otpHash;
    }

    public void setOtpHash(String otpHash) {
        this.otpHash = otpHash;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getHetHan() {
        return hetHan;
    }

    public void setHetHan(LocalDateTime hetHan) {
        this.hetHan = hetHan;
    }

    public LocalDateTime getDaDungLuc() {
        return daDungLuc;
    }

    public void setDaDungLuc(LocalDateTime daDungLuc) {
        this.daDungLuc = daDungLuc;
    }

    public Integer getSoLanNhapSai() {
        return soLanNhapSai;
    }

    public void setSoLanNhapSai(Integer soLanNhapSai) {
        this.soLanNhapSai = soLanNhapSai;
    }

    public Integer getSoLanGui() {
        return soLanGui;
    }

    public void setSoLanGui(Integer soLanGui) {
        this.soLanGui = soLanGui;
    }

    public LocalDateTime getGuiLaiSau() {
        return guiLaiSau;
    }

    public void setGuiLaiSau(LocalDateTime guiLaiSau) {
        this.guiLaiSau = guiLaiSau;
    }

    public byte[] getIpHash() {
        return ipHash;
    }

    public void setIpHash(byte[] ipHash) {
        this.ipHash = ipHash;
    }

    public static YeuCauXacMinhOTPBuilder builder() {
        return new YeuCauXacMinhOTPBuilder();
    }

    public static class YeuCauXacMinhOTPBuilder {
        private UUID maOTP;
        private TaiKhoan taiKhoan;
        private String email;
        private String soDienThoai;
        private String kenhXacMinh = "EMAIL";
        private String mucDich;
        private String otpHash;
        private LocalDateTime ngayTao;
        private LocalDateTime hetHan;
        private LocalDateTime daDungLuc;
        private Integer soLanNhapSai = 0;
        private Integer soLanGui = 1;
        private LocalDateTime guiLaiSau;
        private byte[] ipHash;

        YeuCauXacMinhOTPBuilder() {}

        public YeuCauXacMinhOTPBuilder maOTP(UUID maOTP) {
            this.maOTP = maOTP;
            return this;
        }

        public YeuCauXacMinhOTPBuilder taiKhoan(TaiKhoan taiKhoan) {
            this.taiKhoan = taiKhoan;
            return this;
        }

        public YeuCauXacMinhOTPBuilder email(String email) {
            this.email = email;
            return this;
        }

        public YeuCauXacMinhOTPBuilder soDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
            return this;
        }

        public YeuCauXacMinhOTPBuilder kenhXacMinh(String kenhXacMinh) {
            this.kenhXacMinh = kenhXacMinh;
            return this;
        }

        public YeuCauXacMinhOTPBuilder mucDich(String mucDich) {
            this.mucDich = mucDich;
            return this;
        }

        public YeuCauXacMinhOTPBuilder otpHash(String otpHash) {
            this.otpHash = otpHash;
            return this;
        }

        public YeuCauXacMinhOTPBuilder ngayTao(LocalDateTime ngayTao) {
            this.ngayTao = ngayTao;
            return this;
        }

        public YeuCauXacMinhOTPBuilder hetHan(LocalDateTime hetHan) {
            this.hetHan = hetHan;
            return this;
        }

        public YeuCauXacMinhOTPBuilder daDungLuc(LocalDateTime daDungLuc) {
            this.daDungLuc = daDungLuc;
            return this;
        }

        public YeuCauXacMinhOTPBuilder soLanNhapSai(Integer soLanNhapSai) {
            this.soLanNhapSai = soLanNhapSai;
            return this;
        }

        public YeuCauXacMinhOTPBuilder soLanGui(Integer soLanGui) {
            this.soLanGui = soLanGui;
            return this;
        }

        public YeuCauXacMinhOTPBuilder guiLaiSau(LocalDateTime guiLaiSau) {
            this.guiLaiSau = guiLaiSau;
            return this;
        }

        public YeuCauXacMinhOTPBuilder ipHash(byte[] ipHash) {
            this.ipHash = ipHash;
            return this;
        }

        public YeuCauXacMinhOTP build() {
            return new YeuCauXacMinhOTP(maOTP, taiKhoan, email, soDienThoai, kenhXacMinh, mucDich, otpHash, ngayTao, hetHan, daDungLuc, soLanNhapSai, soLanGui, guiLaiSau, ipHash);
        }
    }
}
