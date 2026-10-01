package com.example.TuVanSinhVien.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TepDinhKem")
@Getter @Setter @NoArgsConstructor
public class TepDinhKem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaTep")
    private Long maTep;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaTK_TaiLen", nullable = false)
    private TaiKhoan taiKhoanTaiLen;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaYeuCau")
    private YeuCauTuVan yeuCauTuVan;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaPhanHoi")
    private PhanHoiTuVan phanHoiTuVan;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaTinNhan")
    private TinNhanChat tinNhanChat;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaBaiViet")
    private BaiVietHuongDan baiVietHuongDan;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "MaFAQ")
    private CauHoiFAQ cauHoiFAQ;
    @Column(name = "TenGoc", nullable = false, length = 255)
    private String tenGoc;
    @Column(name = "MimeType", nullable = false, length = 100)
    private String mimeType;
    @Column(name = "DungLuong", nullable = false)
    private Long dungLuong;
    @Column(name = "CloudinaryAssetId", nullable = false, unique = true, length = 255)
    private String cloudinaryAssetId;
    @Column(name = "CloudinaryPublicId", nullable = false, length = 500)
    private String cloudinaryPublicId;
    @Column(name = "ResourceType", nullable = false, length = 10)
    private String resourceType;
    @Column(name = "DeliveryType", nullable = false, length = 20)
    private String deliveryType;
    @Column(name = "NgayTao", nullable = false)
    private LocalDateTime ngayTao;

    @PrePersist void prePersist() { if (ngayTao == null) ngayTao = LocalDateTime.now(); }
}
