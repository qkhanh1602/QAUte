package com.example.TuVanSinhVien.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TepDinhKem")
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

    public TepDinhKem() {}

    public Long getMaTep() { return maTep; }
    public void setMaTep(Long maTep) { this.maTep = maTep; }

    public TaiKhoan getTaiKhoanTaiLen() { return taiKhoanTaiLen; }
    public void setTaiKhoanTaiLen(TaiKhoan taiKhoanTaiLen) { this.taiKhoanTaiLen = taiKhoanTaiLen; }

    public YeuCauTuVan getYeuCauTuVan() { return yeuCauTuVan; }
    public void setYeuCauTuVan(YeuCauTuVan yeuCauTuVan) { this.yeuCauTuVan = yeuCauTuVan; }

    public PhanHoiTuVan getPhanHoiTuVan() { return phanHoiTuVan; }
    public void setPhanHoiTuVan(PhanHoiTuVan phanHoiTuVan) { this.phanHoiTuVan = phanHoiTuVan; }

    public TinNhanChat getTinNhanChat() { return tinNhanChat; }
    public void setTinNhanChat(TinNhanChat tinNhanChat) { this.tinNhanChat = tinNhanChat; }

    public BaiVietHuongDan getBaiVietHuongDan() { return baiVietHuongDan; }
    public void setBaiVietHuongDan(BaiVietHuongDan baiVietHuongDan) { this.baiVietHuongDan = baiVietHuongDan; }

    public CauHoiFAQ getCauHoiFAQ() { return cauHoiFAQ; }
    public void setCauHoiFAQ(CauHoiFAQ cauHoiFAQ) { this.cauHoiFAQ = cauHoiFAQ; }

    public String getTenGoc() { return tenGoc; }
    public void setTenGoc(String tenGoc) { this.tenGoc = tenGoc; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public Long getDungLuong() { return dungLuong; }
    public void setDungLuong(Long dungLuong) { this.dungLuong = dungLuong; }

    public String getCloudinaryAssetId() { return cloudinaryAssetId; }
    public void setCloudinaryAssetId(String cloudinaryAssetId) { this.cloudinaryAssetId = cloudinaryAssetId; }

    public String getCloudinaryPublicId() { return cloudinaryPublicId; }
    public void setCloudinaryPublicId(String cloudinaryPublicId) { this.cloudinaryPublicId = cloudinaryPublicId; }

    public String getResourceType() { return resourceType; }
    public void setResourceType(String resourceType) { this.resourceType = resourceType; }

    public String getDeliveryType() { return deliveryType; }
    public void setDeliveryType(String deliveryType) { this.deliveryType = deliveryType; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
}
