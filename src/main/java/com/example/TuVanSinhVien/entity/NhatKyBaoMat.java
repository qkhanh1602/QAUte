package com.example.TuVanSinhVien.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "NhatKyBaoMat")
public class NhatKyBaoMat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaSuKien")
    private Long maSuKien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MaTK")
    private TaiKhoan taiKhoan;

    @Column(name = "LoaiSuKien", nullable = false, length = 50)
    private String loaiSuKien;

    @Column(name = "ThanhCong", nullable = false)
    private Boolean thanhCong;

    @Column(name = "IPHash", columnDefinition = "binary(32)")
    private byte[] ipHash;

    @Column(name = "UserAgentHash", columnDefinition = "binary(32)")
    private byte[] userAgentHash;

    @Column(name = "ChiTiet", length = 500)
    private String chiTiet;

    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) ngayTao = LocalDateTime.now();
    }

    public NhatKyBaoMat() {
    }

    public NhatKyBaoMat(Long maSuKien, TaiKhoan taiKhoan, String loaiSuKien, Boolean thanhCong,
                        byte[] ipHash, byte[] userAgentHash, String chiTiet, LocalDateTime ngayTao) {
        this.maSuKien = maSuKien;
        this.taiKhoan = taiKhoan;
        this.loaiSuKien = loaiSuKien;
        this.thanhCong = thanhCong;
        this.ipHash = ipHash;
        this.userAgentHash = userAgentHash;
        this.chiTiet = chiTiet;
        this.ngayTao = ngayTao;
    }

    public Long getMaSuKien() {
        return maSuKien;
    }

    public void setMaSuKien(Long maSuKien) {
        this.maSuKien = maSuKien;
    }

    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public void setTaiKhoan(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
    }

    public String getLoaiSuKien() {
        return loaiSuKien;
    }

    public void setLoaiSuKien(String loaiSuKien) {
        this.loaiSuKien = loaiSuKien;
    }

    public Boolean getThanhCong() {
        return thanhCong;
    }

    public void setThanhCong(Boolean thanhCong) {
        this.thanhCong = thanhCong;
    }

    public byte[] getIpHash() {
        return ipHash;
    }

    public void setIpHash(byte[] ipHash) {
        this.ipHash = ipHash;
    }

    public byte[] getUserAgentHash() {
        return userAgentHash;
    }

    public void setUserAgentHash(byte[] userAgentHash) {
        this.userAgentHash = userAgentHash;
    }

    public String getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(String chiTiet) {
        this.chiTiet = chiTiet;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public static NhatKyBaoMatBuilder builder() {
        return new NhatKyBaoMatBuilder();
    }

    public static class NhatKyBaoMatBuilder {
        private Long maSuKien;
        private TaiKhoan taiKhoan;
        private String loaiSuKien;
        private Boolean thanhCong;
        private byte[] ipHash;
        private byte[] userAgentHash;
        private String chiTiet;
        private LocalDateTime ngayTao;

        NhatKyBaoMatBuilder() {}

        public NhatKyBaoMatBuilder maSuKien(Long maSuKien) {
            this.maSuKien = maSuKien;
            return this;
        }

        public NhatKyBaoMatBuilder taiKhoan(TaiKhoan taiKhoan) {
            this.taiKhoan = taiKhoan;
            return this;
        }

        public NhatKyBaoMatBuilder loaiSuKien(String loaiSuKien) {
            this.loaiSuKien = loaiSuKien;
            return this;
        }

        public NhatKyBaoMatBuilder thanhCong(Boolean thanhCong) {
            this.thanhCong = thanhCong;
            return this;
        }

        public NhatKyBaoMatBuilder ipHash(byte[] ipHash) {
            this.ipHash = ipHash;
            return this;
        }

        public NhatKyBaoMatBuilder userAgentHash(byte[] userAgentHash) {
            this.userAgentHash = userAgentHash;
            return this;
        }

        public NhatKyBaoMatBuilder chiTiet(String chiTiet) {
            this.chiTiet = chiTiet;
            return this;
        }

        public NhatKyBaoMatBuilder ngayTao(LocalDateTime ngayTao) {
            this.ngayTao = ngayTao;
            return this;
        }

        public NhatKyBaoMat build() {
            return new NhatKyBaoMat(maSuKien, taiKhoan, loaiSuKien, thanhCong, ipHash, userAgentHash, chiTiet, ngayTao);
        }
    }
}
