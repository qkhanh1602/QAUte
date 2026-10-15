:ON ERROR EXIT
-- SQLCMD mode. Run ONLY against a new, empty Azure SQL database.
SET XACT_ABORT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
GO
BEGIN TRANSACTION;

CREATE TABLE dbo.TaiKhoan (
MaTK int IDENTITY PRIMARY KEY,
 TenDangNhap varchar(50) NOT NULL UNIQUE,
 MatKhau varchar(255) NOT NULL,
 HoTen nvarchar(100) NOT NULL,
 Email varchar(254) NOT NULL UNIQUE,
 SoDienThoai varchar(15) NULL,
 AvatarUrl varchar(1000) NULL,
 AvatarPublicId varchar(500) NULL,
 VaiTro varchar(20) NOT NULL CHECK (VaiTro IN ('SinhVien','CoVan','Admin')),
 TrangThai varchar(20) NOT NULL DEFAULT 'HoatDong' CHECK (TrangThai IN ('ChoDuyet','HoatDong','Khoa')),
 BatBuocDoiMatKhau bit NOT NULL DEFAULT 1,
 TokenVersion int NOT NULL DEFAULT 0 CHECK (TokenVersion >= 0),
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 NgayCapNhat datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 PhienBan bigint NOT NULL DEFAULT 0
);

CREATE TABLE dbo.DonVi (
MaDonVi int IDENTITY PRIMARY KEY,
 MaDonViCode varchar(30) NOT NULL UNIQUE,
 TenDonVi nvarchar(150) NOT NULL,
 LoaiDonVi varchar(20) NOT NULL CHECK (LoaiDonVi IN ('PhongBan','Khoa','TrungTam')),
 Email varchar(254) NULL, SoDienThoai varchar(15) NULL, MoTa nvarchar(500) NULL,
 TruongDonVi nvarchar(100) NULL,
 MaTK_TruongDonVi int NULL REFERENCES dbo.TaiKhoan(MaTK),
 HoatDong bit NOT NULL DEFAULT 1
);

CREATE TABLE dbo.SinhVien (
MaSV int IDENTITY PRIMARY KEY,
 MSSV varchar(20) NOT NULL UNIQUE,
 Khoa nvarchar(100) NOT NULL, Nganh nvarchar(100) NOT NULL,
 Lop varchar(20) NOT NULL, KhoaHoc varchar(20) NOT NULL,
 MaTK int NOT NULL UNIQUE REFERENCES dbo.TaiKhoan(MaTK)
);

CREATE TABLE dbo.NguoiTuVan (
MaCV int IDENTITY PRIMARY KEY,
 MaCanBo varchar(30) NOT NULL UNIQUE,
 HocHamHocVi nvarchar(50) NULL, ChucVu nvarchar(100) NOT NULL,
 ChuyenMon nvarchar(255) NOT NULL,
 MaDonVi int NOT NULL REFERENCES dbo.DonVi(MaDonVi),
 MaTK int NOT NULL UNIQUE REFERENCES dbo.TaiKhoan(MaTK),
 NhanTuVan bit NOT NULL DEFAULT 1,
 HanNgachTiepNhan int NOT NULL DEFAULT 20 CHECK (HanNgachTiepNhan > 0)
);

CREATE TABLE dbo.LinhVucTuVan (
MaLinhVuc int IDENTITY PRIMARY KEY,
 MaLinhVucCode varchar(30) NOT NULL UNIQUE,
 TenLinhVuc nvarchar(100) NOT NULL, MoTa nvarchar(500) NULL,
 MaDonVi int NOT NULL REFERENCES dbo.DonVi(MaDonVi),
 TrangThai varchar(20) NOT NULL DEFAULT 'HoatDong' CHECK (TrangThai IN ('HoatDong','TamDung')),
 NhayCam bit NOT NULL DEFAULT 0,
 UNIQUE (MaLinhVuc, MaDonVi)
);

CREATE TABLE dbo.YeuCauTuVan (
MaYeuCau int IDENTITY PRIMARY KEY,
 MaSV int NOT NULL REFERENCES dbo.SinhVien(MaSV),
 MaLinhVuc int NOT NULL, MaDonVi int NOT NULL,
 MaCV_PhuTrach int NULL REFERENCES dbo.NguoiTuVan(MaCV),
 TieuDe nvarchar(255) NOT NULL CHECK (LEN(LTRIM(RTRIM(TieuDe))) >= 10),
 NoiDung nvarchar(max) NOT NULL CHECK (LEN(LTRIM(RTRIM(NoiDung))) >= 20),
 BaoMatDanhTinh bit NOT NULL DEFAULT 0,
 TrangThai varchar(20) NOT NULL DEFAULT 'ChoTiepNhan'
  CHECK (TrangThai IN ('ChoTiepNhan','DangXuLy','DaPhanHoi','Dong')),
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 NgayCapNhat datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 HanTiepNhan datetime2(3) NOT NULL, HanSLA datetime2(3) NOT NULL,
 NgayTiepNhan datetime2(3) NULL, NgayPhanHoiDau datetime2(3) NULL,
 NgayPhanHoiCuoi datetime2(3) NULL, HanTuDongDong datetime2(3) NULL,
 NgayDong datetime2(3) NULL, KetLuan nvarchar(2000) NULL,
 PhienBan bigint NOT NULL DEFAULT 0,
 FOREIGN KEY (MaLinhVuc, MaDonVi) REFERENCES dbo.LinhVucTuVan(MaLinhVuc, MaDonVi),
 UNIQUE (MaYeuCau, MaSV),
 CHECK (HanTiepNhan >= NgayTao AND HanSLA >= HanTiepNhan),
 CHECK ((TrangThai = 'Dong' AND NgayDong IS NOT NULL) OR (TrangThai <> 'Dong' AND NgayDong IS NULL))
);

CREATE TABLE dbo.PhanCongTuVan (
MaPhanCong int IDENTITY PRIMARY KEY,
 MaYeuCau int NOT NULL REFERENCES dbo.YeuCauTuVan(MaYeuCau),
 MaTK_ThucHien int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 MaCV_PhuTrach int NOT NULL REFERENCES dbo.NguoiTuVan(MaCV),
 NgayPhanCong datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 NgayKetThuc datetime2(3) NULL, GhiChu nvarchar(500) NULL,
 CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayPhanCong)
);

CREATE TABLE dbo.PhanHoiTuVan (
MaPhanHoi int IDENTITY PRIMARY KEY,
 MaYeuCau int NOT NULL REFERENCES dbo.YeuCauTuVan(MaYeuCau),
 MaTK_Gui int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 LoaiPhanHoi varchar(20) NOT NULL CHECK (LoaiPhanHoi IN ('TraLoi','HoiThem')),
 NoiDungTraLoi nvarchar(max) NOT NULL CHECK (LEN(LTRIM(RTRIM(NoiDungTraLoi))) > 0),
 NgayTraLoi datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 DeXuatFAQ bit NOT NULL DEFAULT 0
);

CREATE TABLE dbo.LichSuYeuCau (
MaLichSu bigint IDENTITY PRIMARY KEY,
 MaYeuCau int NOT NULL REFERENCES dbo.YeuCauTuVan(MaYeuCau),
 MaTK_ThucHien int NULL REFERENCES dbo.TaiKhoan(MaTK),
 HanhDong varchar(30) NOT NULL,
 TrangThaiCu varchar(20) NULL, TrangThaiMoi varchar(20) NULL,
 MaDonViCu int NULL REFERENCES dbo.DonVi(MaDonVi),
 MaDonViMoi int NULL REFERENCES dbo.DonVi(MaDonVi),
 LyDo nvarchar(1000) NULL,
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME()))
);

CREATE TABLE dbo.CauHoiFAQ (
MaFAQ int IDENTITY PRIMARY KEY,
 CauHoi nvarchar(300) NOT NULL, CauTraLoi nvarchar(max) NOT NULL,
 MaLinhVuc int NOT NULL REFERENCES dbo.LinhVucTuVan(MaLinhVuc),
 MaYeuCauNguon int NULL REFERENCES dbo.YeuCauTuVan(MaYeuCau),
 TuKhoa nvarchar(255) NULL, LuotXem int NOT NULL DEFAULT 0 CHECK (LuotXem >= 0),
 TrangThai varchar(20) NOT NULL DEFAULT 'BanNhao' CHECK (TrangThai IN ('BanNhao','ChoDuyet','HienThi','An')),
 MaTK_DeXuat int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 MaTK_Duyet int NULL REFERENCES dbo.TaiKhoan(MaTK),
 NgayDuyet datetime2(3) NULL, NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 CHECK (TrangThai <> 'HienThi' OR (MaTK_Duyet IS NOT NULL AND NgayDuyet IS NOT NULL))
);

CREATE TABLE dbo.BaiVietHuongDan (
MaBaiViet int IDENTITY PRIMARY KEY,
 TieuDe nvarchar(255) NOT NULL, TomTat nvarchar(500) NULL,
 NoiDung nvarchar(max) NOT NULL, TuKhoa nvarchar(255) NULL,
 MaLinhVuc int NOT NULL REFERENCES dbo.LinhVucTuVan(MaLinhVuc),
 MaTK_Admin int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 LuotXem int NOT NULL DEFAULT 0 CHECK (LuotXem >= 0),
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())), NgayDang datetime2(3) NULL,
 MaTK_Duyet int NULL REFERENCES dbo.TaiKhoan(MaTK),
 TrangThai varchar(20) NOT NULL DEFAULT 'BanNhao' CHECK (TrangThai IN ('XuatBan','BanNhao','An')),
 CHECK (TrangThai <> 'XuatBan' OR (NgayDang IS NOT NULL AND MaTK_Duyet IS NOT NULL))
);

CREATE TABLE dbo.MauTraLoi (
MaMau int IDENTITY PRIMARY KEY,
 MaDonVi int NOT NULL REFERENCES dbo.DonVi(MaDonVi),
 TieuDe nvarchar(200) NOT NULL, NoiDung nvarchar(max) NOT NULL,
 MaTK_Tao int NOT NULL REFERENCES dbo.TaiKhoan(MaTK), HoatDong bit NOT NULL DEFAULT 1
);

CREATE TABLE dbo.KhungGioTuVan (
MaKhungGio int IDENTITY PRIMARY KEY,
 MaCV int NOT NULL REFERENCES dbo.NguoiTuVan(MaCV),
 BatDau datetime2(3) NOT NULL, KetThuc datetime2(3) NOT NULL,
 HinhThuc varchar(20) NOT NULL CHECK (HinhThuc IN ('TrucTiep','TrucTuyen')),
 DiaDiem_Link nvarchar(1000) NULL,
 TrangThai varchar(20) NOT NULL DEFAULT 'Mo' CHECK (TrangThai IN ('Mo','Khoa')),
 PhienBan bigint NOT NULL DEFAULT 0,
 CHECK (KetThuc > BatDau),
 CHECK (DATEDIFF(MINUTE, BatDau, KetThuc) IN (45,60)),
 UNIQUE (MaKhungGio, MaCV)
);

CREATE TABLE dbo.LichHenTuVan (
MaLichHen int IDENTITY PRIMARY KEY,
 MaSV int NOT NULL REFERENCES dbo.SinhVien(MaSV),
 MaCV int NOT NULL, MaKhungGio int NOT NULL,
 ChuDe nvarchar(200) NOT NULL,
 NoiDungHen nvarchar(1000) NOT NULL CHECK (LEN(LTRIM(RTRIM(NoiDungHen))) > 0),
 TrangThai varchar(20) NOT NULL DEFAULT 'ChoDuyet'
  CHECK (TrangThai IN ('ChoDuyet','DaXacNhan','TuChoi','HoanThanh','Huy')),
 LyDo nvarchar(500) NULL, LinkHop_Phong nvarchar(1000) NULL,
 KetLuan nvarchar(2000) NULL,
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 HanDuyet datetime2(3) NOT NULL, NgayDuyet datetime2(3) NULL,
 NgayHoanThanh datetime2(3) NULL,
 PhienBan bigint NOT NULL DEFAULT 0,
 FOREIGN KEY (MaKhungGio, MaCV) REFERENCES dbo.KhungGioTuVan(MaKhungGio, MaCV),
 UNIQUE (MaLichHen, MaSV),
 CHECK (TrangThai NOT IN ('TuChoi','Huy') OR (LyDo IS NOT NULL AND LEN(LTRIM(RTRIM(LyDo))) > 0)),
 CHECK (TrangThai NOT IN ('DaXacNhan','HoanThanh') OR (LinkHop_Phong IS NOT NULL AND LEN(LTRIM(RTRIM(LinkHop_Phong))) > 0)),
 CHECK (TrangThai <> 'HoanThanh' OR (NgayHoanThanh IS NOT NULL AND KetLuan IS NOT NULL AND LEN(LTRIM(RTRIM(KetLuan))) > 0))
);

CREATE TABLE dbo.DanhGiaBuoiTuVan (
MaDanhGia int IDENTITY PRIMARY KEY,
 MaLichHen int NOT NULL UNIQUE, MaSV int NOT NULL,
 SoSao tinyint NOT NULL CHECK (SoSao BETWEEN 1 AND 5), NhanXet nvarchar(500) NULL,
 NgayDanhGia datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 FOREIGN KEY (MaLichHen, MaSV) REFERENCES dbo.LichHenTuVan(MaLichHen, MaSV)
);

CREATE TABLE dbo.DanhGiaYeuCau (
MaDanhGia int IDENTITY PRIMARY KEY,
 MaYeuCau int NOT NULL UNIQUE, MaSV int NOT NULL,
 SoSao tinyint NOT NULL CHECK (SoSao BETWEEN 1 AND 5), NhanXet nvarchar(500) NULL,
 NgayDanhGia datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 FOREIGN KEY (MaYeuCau, MaSV) REFERENCES dbo.YeuCauTuVan(MaYeuCau, MaSV)
);

CREATE TABLE dbo.PhienChat (
MaPhienChat int IDENTITY PRIMARY KEY,
 MaSV int NOT NULL REFERENCES dbo.SinhVien(MaSV),
 MaDonVi int NOT NULL REFERENCES dbo.DonVi(MaDonVi),
 MaCV int NULL REFERENCES dbo.NguoiTuVan(MaCV),
 ThoiGianBatDau datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 ThoiGianKetThuc datetime2(3) NULL,
 TrangThai varchar(20) NOT NULL DEFAULT 'ChoTiepNhan'
  CHECK (TrangThai IN ('ChoTiepNhan','DangHoatDong','KetThuc','GianDoan')),
 PhienBan bigint NOT NULL DEFAULT 0,
 CHECK (TrangThai <> 'DangHoatDong' OR MaCV IS NOT NULL),
 CHECK ((TrangThai = 'KetThuc' AND ThoiGianKetThuc IS NOT NULL) OR (TrangThai <> 'KetThuc' AND ThoiGianKetThuc IS NULL)),
 CHECK (ThoiGianKetThuc IS NULL OR ThoiGianKetThuc >= ThoiGianBatDau)
);

CREATE TABLE dbo.TinNhanChat (
MaTinNhan bigint IDENTITY PRIMARY KEY,
 MaPhienChat int NOT NULL REFERENCES dbo.PhienChat(MaPhienChat),
 MaTK_Gui int NULL REFERENCES dbo.TaiKhoan(MaTK),
 ClientMessageId uniqueidentifier NOT NULL,
 LoaiTinNhan varchar(20) NOT NULL DEFAULT 'VanBan' CHECK (LoaiTinNhan IN ('VanBan','TepTin','HinhAnh')),
 NoiDung nvarchar(max) NULL,
 ThoiGianGui datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())), NgayXem datetime2(3) NULL,
 UNIQUE (MaPhienChat, MaTK_Gui, ClientMessageId),
 CHECK (LoaiTinNhan <> 'VanBan' OR (NoiDung IS NOT NULL AND LEN(LTRIM(RTRIM(NoiDung))) > 0)),
 CHECK (NgayXem IS NULL OR NgayXem >= ThoiGianGui)
);

CREATE TABLE dbo.TepDinhKem (
MaTep bigint IDENTITY PRIMARY KEY,
 MaTK_TaiLen int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 MaYeuCau int NULL REFERENCES dbo.YeuCauTuVan(MaYeuCau),
 MaPhanHoi int NULL REFERENCES dbo.PhanHoiTuVan(MaPhanHoi),
 MaTinNhan bigint NULL REFERENCES dbo.TinNhanChat(MaTinNhan),
 MaBaiViet int NULL REFERENCES dbo.BaiVietHuongDan(MaBaiViet),
 MaFAQ int NULL REFERENCES dbo.CauHoiFAQ(MaFAQ),
 TenGoc nvarchar(255) NOT NULL, MimeType varchar(100) NOT NULL,
 DungLuong bigint NOT NULL CHECK (DungLuong > 0 AND DungLuong < 10485760),
 CloudinaryAssetId varchar(255) NOT NULL UNIQUE,
 CloudinaryPublicId varchar(500) NOT NULL,
 ResourceType varchar(10) NOT NULL CHECK (ResourceType IN ('image','raw')),
 DeliveryType varchar(20) NOT NULL CHECK (DeliveryType IN ('upload','private','authenticated')),
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 CHECK ((CASE WHEN MaYeuCau IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN MaPhanHoi IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN MaTinNhan IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN MaBaiViet IS NULL THEN 0 ELSE 1 END) +
        (CASE WHEN MaFAQ IS NULL THEN 0 ELSE 1 END) = 1),
 CHECK ((MaYeuCau IS NULL AND MaPhanHoi IS NULL AND MaTinNhan IS NULL) OR DeliveryType <> 'upload')
);

CREATE TABLE dbo.ThongBao (
MaThongBao bigint IDENTITY PRIMARY KEY,
 MaTK_Nhan int NOT NULL REFERENCES dbo.TaiKhoan(MaTK),
 TieuDe nvarchar(200) NOT NULL, NoiDung nvarchar(500) NOT NULL,
 LoaiThongBao varchar(20) NOT NULL CHECK (LoaiThongBao IN ('LichHen','DienDan','Chatbox','HeThong','SLA')),
 LienKet varchar(500) NULL, DaDoc bit NOT NULL DEFAULT 0,
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 KhoaChongLap varchar(150) NOT NULL,
 UNIQUE (MaTK_Nhan, KhoaChongLap)
);

-- OTP is for a registration/verification flow. Never store the one-time code itself.
CREATE TABLE dbo.YeuCauXacMinhOTP (
 MaOTP uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
 MaTK int NULL REFERENCES dbo.TaiKhoan(MaTK),
 Email varchar(254) NOT NULL,
 SoDienThoai varchar(15) NULL,
 KenhXacMinh varchar(10) NULL,
 MucDich varchar(30) NOT NULL CHECK (MucDich IN ('DangKy','DangKyCoVan','XacMinhEmail','DatLaiMatKhau','DoiEmail')),
 OTPHash varchar(255) NOT NULL,
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 HetHan datetime2(3) NOT NULL,
 DaDungLuc datetime2(3) NULL,
 SoLanNhapSai tinyint NOT NULL DEFAULT 0 CHECK (SoLanNhapSai BETWEEN 0 AND 5),
 SoLanGui tinyint NOT NULL DEFAULT 1 CHECK (SoLanGui BETWEEN 1 AND 3),
 GuiLaiSau datetime2(3) NOT NULL,
 IPHash binary(32) NULL,
 CHECK (HetHan > NgayTao),
 CHECK (GuiLaiSau >= NgayTao),
 CHECK (DaDungLuc IS NULL OR DaDungLuc BETWEEN NgayTao AND HetHan)
);

CREATE TABLE dbo.NhatKyBaoMat (
 MaSuKien bigint IDENTITY PRIMARY KEY,
 MaTK int NULL REFERENCES dbo.TaiKhoan(MaTK),
 LoaiSuKien varchar(50) NOT NULL,
 ThanhCong bit NOT NULL,
 IPHash binary(32) NULL,
 UserAgentHash binary(32) NULL,
 ChiTiet nvarchar(500) NULL,
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME()))
);

CREATE TABLE dbo.CauHinhHeThong (
Khoa varchar(80) NOT NULL PRIMARY KEY,
 GiaTri nvarchar(1000) NOT NULL, MoTa nvarchar(500) NOT NULL,
 MaTK_CapNhat int NULL REFERENCES dbo.TaiKhoan(MaTK),
 NgayCapNhat datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME()))
);

CREATE TABLE dbo.HangDoiSuKien (
MaSuKien bigint IDENTITY PRIMARY KEY,
 LoaiSuKien varchar(60) NOT NULL,
 KhoaChongLap varchar(150) NOT NULL UNIQUE,
 Payload nvarchar(max) NOT NULL CHECK (ISJSON(Payload) = 1),
 NgayTao datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 XuLySau datetime2(3) NOT NULL DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())),
 TrangThai varchar(20) NOT NULL DEFAULT 'ChoXuLy' CHECK (TrangThai IN ('ChoXuLy','DangXuLy','ThanhCong','ThatBai')),
 SoLanThu int NOT NULL DEFAULT 0 CHECK (SoLanThu >= 0),
 KhoaDen datetime2(3) NULL, LoiCuoi nvarchar(1000) NULL
);


CREATE UNIQUE INDEX UX_PhanCong_HienTai ON dbo.PhanCongTuVan(MaYeuCau) WHERE NgayKetThuc IS NULL;
CREATE UNIQUE INDEX UX_LichHen_GiuCho ON dbo.LichHenTuVan(MaKhungGio)
 WHERE TrangThai IN ('ChoDuyet','DaXacNhan','HoanThanh');
CREATE INDEX IX_YeuCau_HangDoi ON dbo.YeuCauTuVan(MaDonVi, TrangThai, NgayTao) INCLUDE(MaCV_PhuTrach, HanSLA);
CREATE INDEX IX_YeuCau_SinhVien ON dbo.YeuCauTuVan(MaSV, NgayTao DESC);
CREATE INDEX IX_YeuCau_SLA ON dbo.YeuCauTuVan(TrangThai, HanSLA) INCLUDE(HanTiepNhan, NgayTiepNhan, NgayPhanHoiDau);
CREATE INDEX IX_PhanHoi_YeuCau ON dbo.PhanHoiTuVan(MaYeuCau, NgayTraLoi, MaPhanHoi);
CREATE INDEX IX_LichSu_YeuCau ON dbo.LichSuYeuCau(MaYeuCau, NgayTao);
CREATE INDEX IX_LichHen_SinhVien ON dbo.LichHenTuVan(MaSV, TrangThai);
CREATE INDEX IX_LichHen_CoVan ON dbo.LichHenTuVan(MaCV, TrangThai);
CREATE INDEX IX_Chat_SinhVien ON dbo.PhienChat(MaSV, ThoiGianBatDau DESC);
CREATE INDEX IX_Chat_HangDoi ON dbo.PhienChat(MaDonVi, TrangThai, ThoiGianBatDau);
CREATE INDEX IX_TinNhan_LichSu ON dbo.TinNhanChat(MaPhienChat, MaTinNhan);
CREATE INDEX IX_ThongBao_ChuaDoc ON dbo.ThongBao(MaTK_Nhan, NgayTao DESC) WHERE DaDoc = 0;
CREATE INDEX IX_OTP_TraCuu ON dbo.YeuCauXacMinhOTP(Email, MucDich, NgayTao DESC)
 INCLUDE(HetHan, DaDungLuc, SoLanNhapSai, GuiLaiSau);
CREATE INDEX IX_NhatKyBaoMat_TaiKhoan ON dbo.NhatKyBaoMat(MaTK, NgayTao DESC);
CREATE INDEX IX_NhatKyBaoMat_IP ON dbo.NhatKyBaoMat(IPHash, NgayTao DESC) WHERE IPHash IS NOT NULL;
CREATE INDEX IX_Outbox_ChoXuLy ON dbo.HangDoiSuKien(TrangThai, XuLySau);
CREATE INDEX IX_FAQ_CongKhai ON dbo.CauHoiFAQ(TrangThai, MaLinhVuc);
CREATE INDEX IX_BaiViet_CongKhai ON dbo.BaiVietHuongDan(TrangThai, MaLinhVuc, NgayDang DESC);
CREATE INDEX IX_Tep_YeuCau ON dbo.TepDinhKem(MaYeuCau) WHERE MaYeuCau IS NOT NULL;
CREATE INDEX IX_Tep_PhanHoi ON dbo.TepDinhKem(MaPhanHoi) WHERE MaPhanHoi IS NOT NULL;
CREATE INDEX IX_Tep_TinNhan ON dbo.TepDinhKem(MaTinNhan) WHERE MaTinNhan IS NOT NULL;
CREATE INDEX IX_Tep_BaiViet ON dbo.TepDinhKem(MaBaiViet) WHERE MaBaiViet IS NOT NULL;
CREATE INDEX IX_Tep_FAQ ON dbo.TepDinhKem(MaFAQ) WHERE MaFAQ IS NOT NULL;

-- Reference data only: no student identities, public passwords or fake university policies.
INSERT dbo.DonVi(MaDonViCode, TenDonVi, LoaiDonVi) VALUES
 ('CNTT',N'Khoa Công nghệ Thông tin','Khoa'),
 ('DAOTAO',N'Phòng Đào tạo','PhongBan'),
 ('CTSV',N'Phòng Công tác Sinh viên','PhongBan'),
 ('HOTRO',N'Trung tâm Hỗ trợ Sinh viên','TrungTam');
INSERT dbo.LinhVucTuVan(MaLinhVucCode, TenLinhVuc, MaDonVi, NhayCam)
 SELECT v.Code, v.Ten, d.MaDonVi, v.NhayCam FROM (VALUES
 ('HOCVU',N'Học vụ và đăng ký học phần','DAOTAO',0),
 ('HOCPHI',N'Học phí và học bổng','CTSV',0),
 ('THUTUC',N'Hồ sơ và thủ tục sinh viên','CTSV',0),
 ('BHYT',N'Bảo hiểm y tế','CTSV',0),
 ('KTX',N'Ký túc xá và đời sống','HOTRO',0),
 ('TAMLY',N'Tư vấn tâm lý','HOTRO',1),
 ('NGHENGHIEP',N'Định hướng nghề nghiệp','CNTT',0)
 ) v(Code,Ten,DonVi,NhayCam) JOIN dbo.DonVi d ON d.MaDonViCode = v.DonVi;
INSERT dbo.CauHinhHeThong(Khoa,GiaTri,MoTa) VALUES
 ('booking.window_days',N'7',N'Cho phép đặt trong 7 ngày tới'),
 ('booking.max_pending',N'2',N'Tối đa 2 lịch chờ duyệt trên mỗi sinh viên'),
 ('booking.cancel_before_hours',N'3',N'Hủy trước ít nhất 3 giờ cho cả hai hình thức'),
 ('booking.approve_hours',N'24',N'Hạn duyệt lịch theo giờ thực'),
 ('booking.allowed_slot_minutes',N'45,60',N'Ưu tiên QĐ24; sửa CHECK nếu đổi thời lượng'),
 ('booking.remind_business_hours',N'2',N'Nhắc trước 2 giờ làm việc'),
 ('ticket.title_min_chars',N'10',N'Tiêu đề tối thiểu 10 ký tự'),
 ('ticket.body_min_chars',N'20',N'Nội dung tối thiểu 20 ký tự'),
 ('ticket.auto_close_days',N'7',N'Đóng sau 7 ngày không có hỏi thêm kể từ phản hồi cuối'),
 ('sla.accept_hours',N'48',N'Hạn tiếp nhận và phản hồi 48 giờ liên tục; cảnh báo khi quá hạn'),
 ('sla.reply_hours',N'48',N'Cảnh báo chưa phản hồi lần đầu'),
 ('sla.clock',N'ELAPSED',N'SLA tính giờ liên tục kể từ khi gửi; giờ trực cố vấn cấu hình riêng'),
 ('session.idle_minutes',N'30',N'Hết phiên sau 30 phút không hoạt động'),
 ('security.otp.expiry_minutes',N'10',N'OTP có hiệu lực 10 phút'),
 ('security.otp.max_verify_attempts',N'5',N'Số lần nhập OTP sai tối đa'),
 ('security.otp.resend_cooldown_seconds',N'60',N'Chờ tối thiểu trước khi gửi lại OTP'),
 ('security.otp.max_sends_per_hour',N'3',N'Số OTP gửi tối đa cho mỗi email/IP mỗi giờ'),
 ('security.login.max_failures',N'5',N'Đăng nhập sai tối đa trước khi khóa tạm'),
 ('security.login.lock_minutes',N'15',N'Thời gian khóa tạm sau nhiều lần đăng nhập sai'),
 ('security.access_token_minutes',N'10',N'JWT ngắn hạn; vẫn kiểm tra phiên bị thu hồi'),
 ('upload.max_bytes_exclusive',N'10485760',N'Tệp phải nhỏ hơn 10 MiB'),
 ('upload.allowed_mime',N'image/jpeg,image/png,application/pdf',N'Kiểm tra chữ ký tệp tại service'),
 ('app.timezone',N'Asia/Ho_Chi_Minh',N'Datetime2 lưu giờ Việt Nam Asia/Ho_Chi_Minh, không cộng thêm 7 giờ khi hiển thị');
COMMIT;
GO

CREATE VIEW dbo.vw_FAQCongKhai AS
 SELECT MaFAQ,CauHoi,CauTraLoi,MaLinhVuc,TuKhoa,LuotXem
 FROM dbo.CauHoiFAQ WHERE TrangThai='HienThi';
GO
CREATE VIEW dbo.vw_BaiVietCongKhai AS
 SELECT MaBaiViet,TieuDe,TomTat,NoiDung,TuKhoa,MaLinhVuc,LuotXem,NgayDang
 FROM dbo.BaiVietHuongDan WHERE TrangThai='XuatBan';
GO
CREATE VIEW dbo.vw_ThongKeYeuCauTheoNgay AS
 SELECT CAST(NgayTao AS date) AS NgayVietNam,MaDonVi,MaLinhVuc,
 COUNT_BIG(*) AS TongYeuCau,
 SUM(CAST(CASE WHEN TrangThai IN ('DaPhanHoi','Dong') THEN 1 ELSE 0 END AS bigint)) AS DaHoanTat,
 CAST(100.0*SUM(CASE WHEN TrangThai IN ('DaPhanHoi','Dong') THEN 1 ELSE 0 END)/NULLIF(COUNT_BIG(*),0) AS decimal(6,2)) AS TyLeHoanTat
 FROM dbo.YeuCauTuVan GROUP BY CAST(NgayTao AS date),MaDonVi,MaLinhVuc;
GO
CREATE TRIGGER dbo.TR_DanhGiaBuoi_HoanThanh ON dbo.DanhGiaBuoiTuVan AFTER INSERT, UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.LichHenTuVan h ON h.MaLichHen=i.MaLichHen WHERE h.TrangThai<>'HoanThanh')
  THROW 51001, 'Only completed appointments can be rated.', 1;
 IF EXISTS (SELECT 1 FROM deleted)
  THROW 51002, 'Submitted appointment ratings are immutable.', 1;
END;
GO
CREATE TRIGGER dbo.TR_DanhGiaYeuCau_DaTraLoi ON dbo.DanhGiaYeuCau AFTER INSERT, UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS (SELECT 1 FROM inserted i JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=i.MaYeuCau WHERE y.TrangThai NOT IN ('DaPhanHoi','Dong'))
  THROW 51003, 'Only answered or closed tickets can be rated.', 1;
 IF EXISTS (SELECT 1 FROM deleted)
  THROW 51004, 'Submitted ticket ratings are immutable.', 1;
END;
GO
CREATE TRIGGER dbo.TR_TinNhan_NguoiThamGia ON dbo.TinNhanChat AFTER INSERT, UPDATE AS
BEGIN
 SET NOCOUNT ON;
 IF EXISTS (
  SELECT 1 FROM inserted i JOIN dbo.PhienChat p ON p.MaPhienChat=i.MaPhienChat
  JOIN dbo.SinhVien s ON s.MaSV=p.MaSV LEFT JOIN dbo.NguoiTuVan c ON c.MaCV=p.MaCV
  WHERE i.MaTK_Gui<>s.MaTK AND (c.MaTK IS NULL OR i.MaTK_Gui<>c.MaTK))
  THROW 51005, 'Sender must be a participant of this chat.', 1;
END;
GO
PRINT N'QAUTE schema and reference data created successfully.';
GO

GO
-- Public forum: existing and new tickets are public; chat and attachments stay private.
IF OBJECT_ID(N'dbo.CauHoiDienDan', N'U') IS NULL
BEGIN
 CREATE TABLE dbo.CauHoiDienDan (
  MaYeuCau int NOT NULL PRIMARY KEY REFERENCES dbo.YeuCauTuVan(MaYeuCau) ON DELETE CASCADE,
  HienThi bit NOT NULL DEFAULT 1,
  LuotXem bigint NOT NULL DEFAULT 0 CHECK (LuotXem >= 0)
 );
 -- One-time publication of existing tickets, authorized by the project owner.
 INSERT INTO dbo.CauHoiDienDan(MaYeuCau) SELECT MaYeuCau FROM dbo.YeuCauTuVan;
END;
GO
