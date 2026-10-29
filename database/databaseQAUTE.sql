SET NOCOUNT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'QAUTE')
BEGIN
    CREATE DATABASE [QAUTE];
END
GO

USE [QAUTE];
GO

IF OBJECT_ID('dbo.vw_BaiVietCongKhai', 'V') IS NOT NULL DROP VIEW dbo.vw_BaiVietCongKhai;
IF OBJECT_ID('dbo.vw_FAQCongKhai', 'V') IS NOT NULL DROP VIEW dbo.vw_FAQCongKhai;
IF OBJECT_ID('dbo.vw_ThongKeYeuCauTheoNgay', 'V') IS NOT NULL DROP VIEW dbo.vw_ThongKeYeuCauTheoNgay;
GO

IF OBJECT_ID('dbo.CauHinhHeThong', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHinhHe__MaTK___00DF2177', 'F') IS NOT NULL ALTER TABLE dbo.[CauHinhHeThong] DROP CONSTRAINT [FK__CauHinhHe__MaTK___00DF2177];
IF OBJECT_ID('dbo.CauHoiDienDan', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHoiDie__MaYeu__01D345B0', 'F') IS NOT NULL ALTER TABLE dbo.[CauHoiDienDan] DROP CONSTRAINT [FK__CauHoiDie__MaYeu__01D345B0];
IF OBJECT_ID('dbo.CauHoiFAQ', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHoiFAQ__MaLin__02C769E9', 'F') IS NOT NULL ALTER TABLE dbo.[CauHoiFAQ] DROP CONSTRAINT [FK__CauHoiFAQ__MaLin__02C769E9];
IF OBJECT_ID('dbo.CauHoiFAQ', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHoiFAQ__MaTK___03BB8E22', 'F') IS NOT NULL ALTER TABLE dbo.[CauHoiFAQ] DROP CONSTRAINT [FK__CauHoiFAQ__MaTK___03BB8E22];
IF OBJECT_ID('dbo.CauHoiFAQ', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHoiFAQ__MaTK___04AFB25B', 'F') IS NOT NULL ALTER TABLE dbo.[CauHoiFAQ] DROP CONSTRAINT [FK__CauHoiFAQ__MaTK___04AFB25B];
IF OBJECT_ID('dbo.CauHoiFAQ', 'U') IS NOT NULL AND OBJECT_ID('FK__CauHoiFAQ__MaYeu__05A3D694', 'F') IS NOT NULL ALTER TABLE dbo.[CauHoiFAQ] DROP CONSTRAINT [FK__CauHoiFAQ__MaYeu__05A3D694];
IF OBJECT_ID('dbo.DanhGiaBuoiTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__DanhGiaBuoiTuVan__0880433F', 'F') IS NOT NULL ALTER TABLE dbo.[DanhGiaBuoiTuVan] DROP CONSTRAINT [FK__DanhGiaBuoiTuVan__0880433F];
IF OBJECT_ID('dbo.DanhGiaYeuCau', 'U') IS NOT NULL AND OBJECT_ID('FK__DanhGiaYeuCau__09746778', 'F') IS NOT NULL ALTER TABLE dbo.[DanhGiaYeuCau] DROP CONSTRAINT [FK__DanhGiaYeuCau__09746778];
IF OBJECT_ID('dbo.DonVi', 'U') IS NOT NULL AND OBJECT_ID('FK__DonVi__MaTK_Truo__0B5CAFEA', 'F') IS NOT NULL ALTER TABLE dbo.[DonVi] DROP CONSTRAINT [FK__DonVi__MaTK_Truo__0B5CAFEA];
IF OBJECT_ID('dbo.KhungGioTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__KhungGioTu__MaCV__0C50D423', 'F') IS NOT NULL ALTER TABLE dbo.[KhungGioTuVan] DROP CONSTRAINT [FK__KhungGioTu__MaCV__0C50D423];
IF OBJECT_ID('dbo.LichHenTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__LichHenTuV__MaSV__0D44F85C', 'F') IS NOT NULL ALTER TABLE dbo.[LichHenTuVan] DROP CONSTRAINT [FK__LichHenTuV__MaSV__0D44F85C];
IF OBJECT_ID('dbo.LichHenTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__LichHenTuVan__0E391C95', 'F') IS NOT NULL ALTER TABLE dbo.[LichHenTuVan] DROP CONSTRAINT [FK__LichHenTuVan__0E391C95];
IF OBJECT_ID('dbo.LichSuYeuCau', 'U') IS NOT NULL AND OBJECT_ID('FK__LichSuYeu__MaDon__0F2D40CE', 'F') IS NOT NULL ALTER TABLE dbo.[LichSuYeuCau] DROP CONSTRAINT [FK__LichSuYeu__MaDon__0F2D40CE];
IF OBJECT_ID('dbo.LichSuYeuCau', 'U') IS NOT NULL AND OBJECT_ID('FK__LichSuYeu__MaDon__10216507', 'F') IS NOT NULL ALTER TABLE dbo.[LichSuYeuCau] DROP CONSTRAINT [FK__LichSuYeu__MaDon__10216507];
IF OBJECT_ID('dbo.LichSuYeuCau', 'U') IS NOT NULL AND OBJECT_ID('FK__LichSuYeu__MaTK___11158940', 'F') IS NOT NULL ALTER TABLE dbo.[LichSuYeuCau] DROP CONSTRAINT [FK__LichSuYeu__MaTK___11158940];
IF OBJECT_ID('dbo.LichSuYeuCau', 'U') IS NOT NULL AND OBJECT_ID('FK__LichSuYeu__MaYeu__1209AD79', 'F') IS NOT NULL ALTER TABLE dbo.[LichSuYeuCau] DROP CONSTRAINT [FK__LichSuYeu__MaYeu__1209AD79];
IF OBJECT_ID('dbo.LinhVucTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__LinhVucTu__MaDon__12FDD1B2', 'F') IS NOT NULL ALTER TABLE dbo.[LinhVucTuVan] DROP CONSTRAINT [FK__LinhVucTu__MaDon__12FDD1B2];
IF OBJECT_ID('dbo.MauTraLoi', 'U') IS NOT NULL AND OBJECT_ID('FK__MauTraLoi__MaDon__13F1F5EB', 'F') IS NOT NULL ALTER TABLE dbo.[MauTraLoi] DROP CONSTRAINT [FK__MauTraLoi__MaDon__13F1F5EB];
IF OBJECT_ID('dbo.MauTraLoi', 'U') IS NOT NULL AND OBJECT_ID('FK__MauTraLoi__MaTK___14E61A24', 'F') IS NOT NULL ALTER TABLE dbo.[MauTraLoi] DROP CONSTRAINT [FK__MauTraLoi__MaTK___14E61A24];
IF OBJECT_ID('dbo.NguoiTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__NguoiTuVa__MaDon__15DA3E5D', 'F') IS NOT NULL ALTER TABLE dbo.[NguoiTuVan] DROP CONSTRAINT [FK__NguoiTuVa__MaDon__15DA3E5D];
IF OBJECT_ID('dbo.NguoiTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__NguoiTuVan__MaTK__16CE6296', 'F') IS NOT NULL ALTER TABLE dbo.[NguoiTuVan] DROP CONSTRAINT [FK__NguoiTuVan__MaTK__16CE6296];
IF OBJECT_ID('dbo.NhatKyBaoMat', 'U') IS NOT NULL AND OBJECT_ID('FK__NhatKyBaoM__MaTK__17C286CF', 'F') IS NOT NULL ALTER TABLE dbo.[NhatKyBaoMat] DROP CONSTRAINT [FK__NhatKyBaoM__MaTK__17C286CF];
IF OBJECT_ID('dbo.PhanCongTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__PhanCongT__MaCV___19AACF41', 'F') IS NOT NULL ALTER TABLE dbo.[PhanCongTuVan] DROP CONSTRAINT [FK__PhanCongT__MaCV___19AACF41];
IF OBJECT_ID('dbo.PhanCongTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__PhanCongT__MaTK___1A9EF37A', 'F') IS NOT NULL ALTER TABLE dbo.[PhanCongTuVan] DROP CONSTRAINT [FK__PhanCongT__MaTK___1A9EF37A];
IF OBJECT_ID('dbo.PhanCongTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__PhanCongT__MaYeu__1B9317B3', 'F') IS NOT NULL ALTER TABLE dbo.[PhanCongTuVan] DROP CONSTRAINT [FK__PhanCongT__MaYeu__1B9317B3];
IF OBJECT_ID('dbo.PhanHoiTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__PhanHoiTu__MaTK___1C873BEC', 'F') IS NOT NULL ALTER TABLE dbo.[PhanHoiTuVan] DROP CONSTRAINT [FK__PhanHoiTu__MaTK___1C873BEC];
IF OBJECT_ID('dbo.PhanHoiTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__PhanHoiTu__MaYeu__1D7B6025', 'F') IS NOT NULL ALTER TABLE dbo.[PhanHoiTuVan] DROP CONSTRAINT [FK__PhanHoiTu__MaYeu__1D7B6025];
IF OBJECT_ID('dbo.PhienChat', 'U') IS NOT NULL AND OBJECT_ID('FK__PhienChat__MaCV__1E6F845E', 'F') IS NOT NULL ALTER TABLE dbo.[PhienChat] DROP CONSTRAINT [FK__PhienChat__MaCV__1E6F845E];
IF OBJECT_ID('dbo.PhienChat', 'U') IS NOT NULL AND OBJECT_ID('FK__PhienChat__MaDon__1F63A897', 'F') IS NOT NULL ALTER TABLE dbo.[PhienChat] DROP CONSTRAINT [FK__PhienChat__MaDon__1F63A897];
IF OBJECT_ID('dbo.PhienChat', 'U') IS NOT NULL AND OBJECT_ID('FK__PhienChat__MaSV__2057CCD0', 'F') IS NOT NULL ALTER TABLE dbo.[PhienChat] DROP CONSTRAINT [FK__PhienChat__MaSV__2057CCD0];
IF OBJECT_ID('dbo.SinhVien', 'U') IS NOT NULL AND OBJECT_ID('FK__SinhVien__MaTK__22401542', 'F') IS NOT NULL ALTER TABLE dbo.[SinhVien] DROP CONSTRAINT [FK__SinhVien__MaTK__22401542];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaBai__2334397B', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaBai__2334397B];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaFAQ__24285DB4', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaFAQ__24285DB4];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaPha__251C81ED', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaPha__251C81ED];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaTin__2610A626', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaTin__2610A626];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaTK___2704CA5F', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaTK___2704CA5F];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL AND OBJECT_ID('FK__TepDinhKe__MaYeu__27F8EE98', 'F') IS NOT NULL ALTER TABLE dbo.[TepDinhKem] DROP CONSTRAINT [FK__TepDinhKe__MaYeu__27F8EE98];
IF OBJECT_ID('dbo.ThongBao', 'U') IS NOT NULL AND OBJECT_ID('FK__ThongBao__MaTK_N__28ED12D1', 'F') IS NOT NULL ALTER TABLE dbo.[ThongBao] DROP CONSTRAINT [FK__ThongBao__MaTK_N__28ED12D1];
IF OBJECT_ID('dbo.TinNhanChat', 'U') IS NOT NULL AND OBJECT_ID('FK__TinNhanCh__MaPhi__29E1370A', 'F') IS NOT NULL ALTER TABLE dbo.[TinNhanChat] DROP CONSTRAINT [FK__TinNhanCh__MaPhi__29E1370A];
IF OBJECT_ID('dbo.TinNhanChat', 'U') IS NOT NULL AND OBJECT_ID('FK__TinNhanCh__MaTK___2AD55B43', 'F') IS NOT NULL ALTER TABLE dbo.[TinNhanChat] DROP CONSTRAINT [FK__TinNhanCh__MaTK___2AD55B43];
IF OBJECT_ID('dbo.YeuCauTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__YeuCauTuV__MaCV___2CBDA3B5', 'F') IS NOT NULL ALTER TABLE dbo.[YeuCauTuVan] DROP CONSTRAINT [FK__YeuCauTuV__MaCV___2CBDA3B5];
IF OBJECT_ID('dbo.YeuCauTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__YeuCauTuVa__MaSV__2DB1C7EE', 'F') IS NOT NULL ALTER TABLE dbo.[YeuCauTuVan] DROP CONSTRAINT [FK__YeuCauTuVa__MaSV__2DB1C7EE];
IF OBJECT_ID('dbo.YeuCauTuVan', 'U') IS NOT NULL AND OBJECT_ID('FK__YeuCauTuVan__2EA5EC27', 'F') IS NOT NULL ALTER TABLE dbo.[YeuCauTuVan] DROP CONSTRAINT [FK__YeuCauTuVan__2EA5EC27];
IF OBJECT_ID('dbo.YeuCauXacMinhOTP', 'U') IS NOT NULL AND OBJECT_ID('FK__YeuCauXacM__MaTK__2F9A1060', 'F') IS NOT NULL ALTER TABLE dbo.[YeuCauXacMinhOTP] DROP CONSTRAINT [FK__YeuCauXacM__MaTK__2F9A1060];
IF OBJECT_ID('dbo.BaiVietHuongDan', 'U') IS NOT NULL AND OBJECT_ID('FK__BaiVietHu__MaLin__7E02B4CC', 'F') IS NOT NULL ALTER TABLE dbo.[BaiVietHuongDan] DROP CONSTRAINT [FK__BaiVietHu__MaLin__7E02B4CC];
IF OBJECT_ID('dbo.BaiVietHuongDan', 'U') IS NOT NULL AND OBJECT_ID('FK__BaiVietHu__MaTK___7EF6D905', 'F') IS NOT NULL ALTER TABLE dbo.[BaiVietHuongDan] DROP CONSTRAINT [FK__BaiVietHu__MaTK___7EF6D905];
IF OBJECT_ID('dbo.BaiVietHuongDan', 'U') IS NOT NULL AND OBJECT_ID('FK__BaiVietHu__MaTK___7FEAFD3E', 'F') IS NOT NULL ALTER TABLE dbo.[BaiVietHuongDan] DROP CONSTRAINT [FK__BaiVietHu__MaTK___7FEAFD3E];
GO

IF OBJECT_ID('dbo.HangDoiSuKien', 'U') IS NOT NULL DROP TABLE dbo.[HangDoiSuKien];
IF OBJECT_ID('dbo.YeuCauXacMinhOTP', 'U') IS NOT NULL DROP TABLE dbo.[YeuCauXacMinhOTP];
IF OBJECT_ID('dbo.NhatKyBaoMat', 'U') IS NOT NULL DROP TABLE dbo.[NhatKyBaoMat];
IF OBJECT_ID('dbo.ThongBao', 'U') IS NOT NULL DROP TABLE dbo.[ThongBao];
IF OBJECT_ID('dbo.TepDinhKem', 'U') IS NOT NULL DROP TABLE dbo.[TepDinhKem];
IF OBJECT_ID('dbo.TinNhanChat', 'U') IS NOT NULL DROP TABLE dbo.[TinNhanChat];
IF OBJECT_ID('dbo.PhienChat', 'U') IS NOT NULL DROP TABLE dbo.[PhienChat];
IF OBJECT_ID('dbo.DanhGiaBuoiTuVan', 'U') IS NOT NULL DROP TABLE dbo.[DanhGiaBuoiTuVan];
IF OBJECT_ID('dbo.LichHenTuVan', 'U') IS NOT NULL DROP TABLE dbo.[LichHenTuVan];
IF OBJECT_ID('dbo.KhungGioTuVan', 'U') IS NOT NULL DROP TABLE dbo.[KhungGioTuVan];
IF OBJECT_ID('dbo.BaiVietHuongDan', 'U') IS NOT NULL DROP TABLE dbo.[BaiVietHuongDan];
IF OBJECT_ID('dbo.CauHoiFAQ', 'U') IS NOT NULL DROP TABLE dbo.[CauHoiFAQ];
IF OBJECT_ID('dbo.LichSuYeuCau', 'U') IS NOT NULL DROP TABLE dbo.[LichSuYeuCau];
IF OBJECT_ID('dbo.DanhGiaYeuCau', 'U') IS NOT NULL DROP TABLE dbo.[DanhGiaYeuCau];
IF OBJECT_ID('dbo.PhanHoiTuVan', 'U') IS NOT NULL DROP TABLE dbo.[PhanHoiTuVan];
IF OBJECT_ID('dbo.PhanCongTuVan', 'U') IS NOT NULL DROP TABLE dbo.[PhanCongTuVan];
IF OBJECT_ID('dbo.CauHoiDienDan', 'U') IS NOT NULL DROP TABLE dbo.[CauHoiDienDan];
IF OBJECT_ID('dbo.YeuCauTuVan', 'U') IS NOT NULL DROP TABLE dbo.[YeuCauTuVan];
IF OBJECT_ID('dbo.MauTraLoi', 'U') IS NOT NULL DROP TABLE dbo.[MauTraLoi];
IF OBJECT_ID('dbo.CauHinhHeThong', 'U') IS NOT NULL DROP TABLE dbo.[CauHinhHeThong];
IF OBJECT_ID('dbo.LinhVucTuVan', 'U') IS NOT NULL DROP TABLE dbo.[LinhVucTuVan];
IF OBJECT_ID('dbo.NguoiTuVan', 'U') IS NOT NULL DROP TABLE dbo.[NguoiTuVan];
IF OBJECT_ID('dbo.SinhVien', 'U') IS NOT NULL DROP TABLE dbo.[SinhVien];
IF OBJECT_ID('dbo.DonVi', 'U') IS NOT NULL DROP TABLE dbo.[DonVi];
IF OBJECT_ID('dbo.TaiKhoan', 'U') IS NOT NULL DROP TABLE dbo.[TaiKhoan];
GO

CREATE TABLE [dbo].[TaiKhoan] (
    [MaTK] int IDENTITY(1,1) NOT NULL,
    [TenDangNhap] varchar(50) NOT NULL,
    [MatKhau] varchar(255) NOT NULL,
    [HoTen] nvarchar(100) NOT NULL,
    [Email] varchar(254) NOT NULL,
    [SoDienThoai] varchar(15) NULL,
    [VaiTro] varchar(20) NOT NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('HoatDong'),
    [BatBuocDoiMatKhau] bit NOT NULL DEFAULT ((1)),
    [TokenVersion] int NOT NULL DEFAULT ((0)),
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [NgayCapNhat] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [PhienBan] bigint NOT NULL DEFAULT ((0)),
    [AvatarUrl] varchar(1000) NULL,
    [AvatarPublicId] varchar(500) NULL,
    CONSTRAINT [PK_TaiKhoan] PRIMARY KEY CLUSTERED ([MaTK] ASC),
    CONSTRAINT [UQ__TaiKhoan__55F68FC02969A854] UNIQUE ([TenDangNhap] ASC),
    CONSTRAINT [UQ__TaiKhoan__A9D1053486D4A738] UNIQUE ([Email] ASC),
    CONSTRAINT [CK__TaiKhoan__TokenV__50FB042B] CHECK ([TokenVersion]>=(0)),
    CONSTRAINT [CK__TaiKhoan__VaiTro__51EF2864] CHECK ([VaiTro]='Admin' OR [VaiTro]='CoVan' OR [VaiTro]='SinhVien'),
    CONSTRAINT [CK_TaiKhoan_TrangThai] CHECK ([TrangThai]='Khoa' OR [TrangThai]='HoatDong' OR [TrangThai]='ChoDuyet')
);
GO

CREATE TABLE [dbo].[DonVi] (
    [MaDonVi] int IDENTITY(1,1) NOT NULL,
    [MaDonViCode] varchar(30) NOT NULL,
    [TenDonVi] nvarchar(150) NOT NULL,
    [LoaiDonVi] varchar(20) NOT NULL,
    [Email] varchar(254) NULL,
    [SoDienThoai] varchar(15) NULL,
    [MoTa] nvarchar(500) NULL,
    [TruongDonVi] nvarchar(100) NULL,
    [MaTK_TruongDonVi] int NULL,
    [HoatDong] bit NOT NULL DEFAULT ((1)),
    CONSTRAINT [PK_DonVi] PRIMARY KEY CLUSTERED ([MaDonVi] ASC),
    CONSTRAINT [UQ__DonVi__ED4AB4520AFA3172] UNIQUE ([MaDonViCode] ASC),
    CONSTRAINT [CK__DonVi__LoaiDonVi__39237A9A] CHECK ([LoaiDonVi]='TrungTam' OR [LoaiDonVi]='Khoa' OR [LoaiDonVi]='PhongBan')
);
GO

CREATE TABLE [dbo].[SinhVien] (
    [MaSV] int IDENTITY(1,1) NOT NULL,
    [MSSV] varchar(20) NOT NULL,
    [Khoa] nvarchar(100) NOT NULL,
    [Nganh] nvarchar(100) NOT NULL,
    [Lop] varchar(20) NOT NULL,
    [KhoaHoc] varchar(20) NOT NULL,
    [MaTK] int NOT NULL,
    CONSTRAINT [PK_SinhVien] PRIMARY KEY CLUSTERED ([MaSV] ASC),
    CONSTRAINT [UQ__SinhVien__27250071ED53E0FC] UNIQUE ([MaTK] ASC),
    CONSTRAINT [UQ__SinhVien__6CB3B7F8E9BEAB71] UNIQUE ([MSSV] ASC)
);
GO

CREATE TABLE [dbo].[NguoiTuVan] (
    [MaCV] int IDENTITY(1,1) NOT NULL,
    [MaCanBo] varchar(30) NOT NULL,
    [HocHamHocVi] nvarchar(50) NULL,
    [ChucVu] nvarchar(100) NOT NULL,
    [ChuyenMon] nvarchar(255) NOT NULL,
    [MaDonVi] int NOT NULL,
    [MaTK] int NOT NULL,
    [NhanTuVan] bit NOT NULL DEFAULT ((1)),
    [HanNgachTiepNhan] int NOT NULL DEFAULT ((20)),
    CONSTRAINT [PK_NguoiTuVan] PRIMARY KEY CLUSTERED ([MaCV] ASC),
    CONSTRAINT [UQ__NguoiTuV__272500717DDA2BA3] UNIQUE ([MaTK] ASC),
    CONSTRAINT [UQ__NguoiTuV__4003E2146EFD9652] UNIQUE ([MaCanBo] ASC),
    CONSTRAINT [CK__NguoiTuVa__HanNg__4865BE2A] CHECK ([HanNgachTiepNhan]>(0))
);
GO

CREATE TABLE [dbo].[LinhVucTuVan] (
    [MaLinhVuc] int IDENTITY(1,1) NOT NULL,
    [MaLinhVucCode] varchar(30) NOT NULL,
    [TenLinhVuc] nvarchar(100) NOT NULL,
    [MoTa] nvarchar(500) NULL,
    [MaDonVi] int NOT NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('HoatDong'),
    [NhayCam] bit NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_LinhVucTuVan] PRIMARY KEY CLUSTERED ([MaLinhVuc] ASC),
    CONSTRAINT [UQ__LinhVucT__2400B27406034BA3] UNIQUE ([MaLinhVucCode] ASC),
    CONSTRAINT [UQ__LinhVucT__DC52CECD30D69A51] UNIQUE ([MaLinhVuc] ASC, [MaDonVi] ASC),
    CONSTRAINT [CK__LinhVucTu__Trang__477199F1] CHECK ([TrangThai]='TamDung' OR [TrangThai]='HoatDong')
);
GO

CREATE TABLE [dbo].[CauHinhHeThong] (
    [Khoa] varchar(80) NOT NULL,
    [GiaTri] nvarchar(1000) NOT NULL,
    [MoTa] nvarchar(500) NOT NULL,
    [MaTK_CapNhat] int NULL,
    [NgayCapNhat] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_CauHinhHeThong] PRIMARY KEY CLUSTERED ([Khoa] ASC)
);
GO

CREATE TABLE [dbo].[MauTraLoi] (
    [MaMau] int IDENTITY(1,1) NOT NULL,
    [MaDonVi] int NOT NULL,
    [TieuDe] nvarchar(200) NOT NULL,
    [NoiDung] nvarchar(max) NOT NULL,
    [MaTK_Tao] int NOT NULL,
    [HoatDong] bit NOT NULL DEFAULT ((1)),
    CONSTRAINT [PK_MauTraLoi] PRIMARY KEY CLUSTERED ([MaMau] ASC)
);
GO

CREATE TABLE [dbo].[YeuCauTuVan] (
    [MaYeuCau] int IDENTITY(1,1) NOT NULL,
    [MaSV] int NOT NULL,
    [MaLinhVuc] int NOT NULL,
    [MaDonVi] int NOT NULL,
    [MaCV_PhuTrach] int NULL,
    [TieuDe] nvarchar(255) NOT NULL,
    [NoiDung] nvarchar(max) NOT NULL,
    [BaoMatDanhTinh] bit NOT NULL DEFAULT ((0)),
    [TrangThai] varchar(20) NOT NULL DEFAULT ('ChoTiepNhan'),
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [NgayCapNhat] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [HanTiepNhan] datetime2(3) NOT NULL,
    [HanSLA] datetime2(3) NOT NULL,
    [NgayTiepNhan] datetime2(3) NULL,
    [NgayPhanHoiDau] datetime2(3) NULL,
    [NgayPhanHoiCuoi] datetime2(3) NULL,
    [HanTuDongDong] datetime2(3) NULL,
    [NgayDong] datetime2(3) NULL,
    [KetLuan] nvarchar(2000) NULL,
    [PhienBan] bigint NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_YeuCauTuVan] PRIMARY KEY CLUSTERED ([MaYeuCau] ASC),
    CONSTRAINT [UQ__YeuCauTu__7DD78FCE0FBDE4BF] UNIQUE ([MaYeuCau] ASC, [MaSV] ASC),
    CONSTRAINT [CK__YeuCauTuV__NoiDu__5D60DB10] CHECK (len(ltrim(rtrim([NoiDung])))>=(20)),
    CONSTRAINT [CK__YeuCauTuV__TieuD__5E54FF49] CHECK (len(ltrim(rtrim([TieuDe])))>=(10)),
    CONSTRAINT [CK__YeuCauTuV__Trang__5F492382] CHECK ([TrangThai]='Dong' OR [TrangThai]='DaPhanHoi' OR [TrangThai]='DangXuLy' OR [TrangThai]='ChoTiepNhan'),
    CONSTRAINT [CK__YeuCauTuVan__603D47BB] CHECK ([HanTiepNhan]>=[NgayTao] AND [HanSLA]>=[HanTiepNhan]),
    CONSTRAINT [CK__YeuCauTuVan__61316BF4] CHECK ([TrangThai]='Dong' AND [NgayDong] IS NOT NULL OR [TrangThai]<>'Dong' AND [NgayDong] IS NULL)
);
GO

CREATE TABLE [dbo].[CauHoiDienDan] (
    [MaYeuCau] int NOT NULL,
    [HienThi] bit NOT NULL DEFAULT ((1)),
    [LuotXem] bigint NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_CauHoiDienDan] PRIMARY KEY CLUSTERED ([MaYeuCau] ASC),
    CONSTRAINT [CK__CauHoiDie__LuotX__336AA144] CHECK ([LuotXem]>=(0))
);
GO

CREATE TABLE [dbo].[PhanCongTuVan] (
    [MaPhanCong] int IDENTITY(1,1) NOT NULL,
    [MaYeuCau] int NOT NULL,
    [MaTK_ThucHien] int NOT NULL,
    [MaCV_PhuTrach] int NOT NULL,
    [NgayPhanCong] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [NgayKetThuc] datetime2(3) NULL,
    [GhiChu] nvarchar(500) NULL,
    CONSTRAINT [PK_PhanCongTuVan] PRIMARY KEY CLUSTERED ([MaPhanCong] ASC),
    CONSTRAINT [CK__PhanCongTuVan__4959E263] CHECK ([NgayKetThuc] IS NULL OR [NgayKetThuc]>=[NgayPhanCong])
);
GO

CREATE TABLE [dbo].[PhanHoiTuVan] (
    [MaPhanHoi] int IDENTITY(1,1) NOT NULL,
    [MaYeuCau] int NOT NULL,
    [MaTK_Gui] int NOT NULL,
    [LoaiPhanHoi] varchar(20) NOT NULL,
    [NoiDungTraLoi] nvarchar(max) NOT NULL,
    [NgayTraLoi] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [DeXuatFAQ] bit NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_PhanHoiTuVan] PRIMARY KEY CLUSTERED ([MaPhanHoi] ASC),
    CONSTRAINT [CK__PhanHoiTu__LoaiP__4A4E069C] CHECK ([LoaiPhanHoi]='HoiThem' OR [LoaiPhanHoi]='TraLoi'),
    CONSTRAINT [CK__PhanHoiTu__NoiDu__4B422AD5] CHECK (len(ltrim(rtrim([NoiDungTraLoi])))>(0))
);
GO

CREATE TABLE [dbo].[DanhGiaYeuCau] (
    [MaDanhGia] int IDENTITY(1,1) NOT NULL,
    [MaYeuCau] int NOT NULL,
    [MaSV] int NOT NULL,
    [SoSao] tinyint NOT NULL,
    [NhanXet] nvarchar(500) NULL,
    [NgayDanhGia] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_DanhGiaYeuCau] PRIMARY KEY CLUSTERED ([MaDanhGia] ASC),
    CONSTRAINT [UQ__DanhGiaY__CFA5DF4F6F318C7F] UNIQUE ([MaYeuCau] ASC),
    CONSTRAINT [CK__DanhGiaYe__SoSao__382F5661] CHECK ([SoSao]>=(1) AND [SoSao]<=(5))
);
GO

CREATE TABLE [dbo].[LichSuYeuCau] (
    [MaLichSu] bigint IDENTITY(1,1) NOT NULL,
    [MaYeuCau] int NOT NULL,
    [MaTK_ThucHien] int NULL,
    [HanhDong] varchar(30) NOT NULL,
    [TrangThaiCu] varchar(20) NULL,
    [TrangThaiMoi] varchar(20) NULL,
    [MaDonViCu] int NULL,
    [MaDonViMoi] int NULL,
    [LyDo] nvarchar(1000) NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_LichSuYeuCau] PRIMARY KEY CLUSTERED ([MaLichSu] ASC)
);
GO

CREATE TABLE [dbo].[CauHoiFAQ] (
    [MaFAQ] int IDENTITY(1,1) NOT NULL,
    [CauHoi] nvarchar(300) NOT NULL,
    [CauTraLoi] nvarchar(max) NOT NULL,
    [MaLinhVuc] int NOT NULL,
    [MaYeuCauNguon] int NULL,
    [TuKhoa] nvarchar(255) NULL,
    [LuotXem] int NOT NULL DEFAULT ((0)),
    [TrangThai] varchar(20) NOT NULL DEFAULT ('BanNhao'),
    [MaTK_DeXuat] int NOT NULL,
    [MaTK_Duyet] int NULL,
    [NgayDuyet] datetime2(3) NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_CauHoiFAQ] PRIMARY KEY CLUSTERED ([MaFAQ] ASC),
    CONSTRAINT [CK__CauHoiFAQ__345EC57D] CHECK ([TrangThai]<>'HienThi' OR [MaTK_Duyet] IS NOT NULL AND [NgayDuyet] IS NOT NULL),
    CONSTRAINT [CK__CauHoiFAQ__LuotX__3552E9B6] CHECK ([LuotXem]>=(0)),
    CONSTRAINT [CK__CauHoiFAQ__Trang__36470DEF] CHECK ([TrangThai]='An' OR [TrangThai]='HienThi' OR [TrangThai]='ChoDuyet' OR [TrangThai]='BanNhao')
);
GO

CREATE TABLE [dbo].[BaiVietHuongDan] (
    [MaBaiViet] int IDENTITY(1,1) NOT NULL,
    [TieuDe] nvarchar(255) NOT NULL,
    [TomTat] nvarchar(500) NULL,
    [NoiDung] nvarchar(max) NOT NULL,
    [TuKhoa] nvarchar(255) NULL,
    [MaLinhVuc] int NOT NULL,
    [MaTK_Admin] int NOT NULL,
    [LuotXem] int NOT NULL DEFAULT ((0)),
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [NgayDang] datetime2(3) NULL,
    [MaTK_Duyet] int NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('BanNhao'),
    CONSTRAINT [PK_BaiVietHuongDan] PRIMARY KEY CLUSTERED ([MaBaiViet] ASC),
    CONSTRAINT [CK__BaiVietHu__LuotX__308E3499] CHECK ([LuotXem]>=(0)),
    CONSTRAINT [CK__BaiVietHu__Trang__318258D2] CHECK ([TrangThai]='An' OR [TrangThai]='BanNhao' OR [TrangThai]='XuatBan'),
    CONSTRAINT [CK__BaiVietHuongDan__32767D0B] CHECK ([TrangThai]<>'XuatBan' OR [NgayDang] IS NOT NULL AND [MaTK_Duyet] IS NOT NULL)
);
GO

CREATE TABLE [dbo].[KhungGioTuVan] (
    [MaKhungGio] int IDENTITY(1,1) NOT NULL,
    [MaCV] int NOT NULL,
    [BatDau] datetime2(3) NOT NULL,
    [KetThuc] datetime2(3) NOT NULL,
    [HinhThuc] varchar(20) NOT NULL,
    [DiaDiem_Link] nvarchar(1000) NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('Mo'),
    [PhienBan] bigint NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_KhungGioTuVan] PRIMARY KEY CLUSTERED ([MaKhungGio] ASC),
    CONSTRAINT [UQ__KhungGio__6CB32A8FCC0D680F] UNIQUE ([MaKhungGio] ASC, [MaCV] ASC),
    CONSTRAINT [CK__KhungGioT__HinhT__3CF40B7E] CHECK ([HinhThuc]='TrucTuyen' OR [HinhThuc]='TrucTiep'),
    CONSTRAINT [CK__KhungGioT__Trang__3DE82FB7] CHECK ([TrangThai]='Khoa' OR [TrangThai]='Mo'),
    CONSTRAINT [CK__KhungGioTuVan__3EDC53F0] CHECK ([KetThuc]>[BatDau]),
    CONSTRAINT [CK__KhungGioTuVan__3FD07829] CHECK (datediff(minute,[BatDau],[KetThuc])=(60) OR datediff(minute,[BatDau],[KetThuc])=(45))
);
GO

CREATE TABLE [dbo].[LichHenTuVan] (
    [MaLichHen] int IDENTITY(1,1) NOT NULL,
    [MaSV] int NOT NULL,
    [MaCV] int NOT NULL,
    [MaKhungGio] int NOT NULL,
    [ChuDe] nvarchar(200) NOT NULL,
    [NoiDungHen] nvarchar(1000) NOT NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('ChoDuyet'),
    [LyDo] nvarchar(500) NULL,
    [LinkHop_Phong] nvarchar(1000) NULL,
    [KetLuan] nvarchar(2000) NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [HanDuyet] datetime2(3) NOT NULL,
    [NgayDuyet] datetime2(3) NULL,
    [NgayHoanThanh] datetime2(3) NULL,
    [PhienBan] bigint NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_LichHenTuVan] PRIMARY KEY CLUSTERED ([MaLichHen] ASC),
    CONSTRAINT [UQ__LichHenT__A77D76CFFC418D4F] UNIQUE ([MaLichHen] ASC, [MaSV] ASC),
    CONSTRAINT [CK__LichHenTu__NoiDu__40C49C62] CHECK (len(ltrim(rtrim([NoiDungHen])))>(0)),
    CONSTRAINT [CK__LichHenTu__Trang__41B8C09B] CHECK ([TrangThai]='Huy' OR [TrangThai]='HoanThanh' OR [TrangThai]='TuChoi' OR [TrangThai]='DaXacNhan' OR [TrangThai]='ChoDuyet'),
    CONSTRAINT [CK__LichHenTuVan__42ACE4D4] CHECK (NOT ([TrangThai]='Huy' OR [TrangThai]='TuChoi') OR [LyDo] IS NOT NULL AND len(ltrim(rtrim([LyDo])))>(0)),
    CONSTRAINT [CK__LichHenTuVan__43A1090D] CHECK (NOT ([TrangThai]='HoanThanh' OR [TrangThai]='DaXacNhan') OR [LinkHop_Phong] IS NOT NULL AND len(ltrim(rtrim([LinkHop_Phong])))>(0)),
    CONSTRAINT [CK__LichHenTuVan__44952D46] CHECK ([TrangThai]<>'HoanThanh' OR [NgayHoanThanh] IS NOT NULL AND [KetLuan] IS NOT NULL AND len(ltrim(rtrim([KetLuan])))>(0))
);
GO

CREATE TABLE [dbo].[DanhGiaBuoiTuVan] (
    [MaDanhGia] int IDENTITY(1,1) NOT NULL,
    [MaLichHen] int NOT NULL,
    [MaSV] int NOT NULL,
    [SoSao] tinyint NOT NULL,
    [NhanXet] nvarchar(500) NULL,
    [NgayDanhGia] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_DanhGiaBuoiTuVan] PRIMARY KEY CLUSTERED ([MaDanhGia] ASC),
    CONSTRAINT [UQ__DanhGiaB__150F264E34A89551] UNIQUE ([MaLichHen] ASC),
    CONSTRAINT [CK__DanhGiaBu__SoSao__373B3228] CHECK ([SoSao]>=(1) AND [SoSao]<=(5))
);
GO

CREATE TABLE [dbo].[PhienChat] (
    [MaPhienChat] int IDENTITY(1,1) NOT NULL,
    [MaSV] int NOT NULL,
    [MaDonVi] int NOT NULL,
    [MaCV] int NULL,
    [ThoiGianBatDau] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [ThoiGianKetThuc] datetime2(3) NULL,
    [TrangThai] varchar(20) NOT NULL DEFAULT ('ChoTiepNhan'),
    [PhienBan] bigint NOT NULL DEFAULT ((0)),
    CONSTRAINT [PK_PhienChat] PRIMARY KEY CLUSTERED ([MaPhienChat] ASC),
    CONSTRAINT [CK__PhienChat__4C364F0E] CHECK ([TrangThai]<>'DangHoatDong' OR [MaCV] IS NOT NULL),
    CONSTRAINT [CK__PhienChat__4D2A7347] CHECK ([TrangThai]='KetThuc' AND [ThoiGianKetThuc] IS NOT NULL OR [TrangThai]<>'KetThuc' AND [ThoiGianKetThuc] IS NULL),
    CONSTRAINT [CK__PhienChat__4E1E9780] CHECK ([ThoiGianKetThuc] IS NULL OR [ThoiGianKetThuc]>=[ThoiGianBatDau]),
    CONSTRAINT [CK__PhienChat__Trang__4F12BBB9] CHECK ([TrangThai]='GianDoan' OR [TrangThai]='KetThuc' OR [TrangThai]='DangHoatDong' OR [TrangThai]='ChoTiepNhan')
);
GO

CREATE TABLE [dbo].[TinNhanChat] (
    [MaTinNhan] bigint IDENTITY(1,1) NOT NULL,
    [MaPhienChat] int NOT NULL,
    [MaTK_Gui] int NULL,
    [ClientMessageId] uniqueidentifier NOT NULL,
    [LoaiTinNhan] varchar(20) NOT NULL DEFAULT ('VanBan'),
    [NoiDung] nvarchar(max) NULL,
    [ThoiGianGui] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [NgayXem] datetime2(3) NULL,
    CONSTRAINT [PK_TinNhanChat] PRIMARY KEY CLUSTERED ([MaTinNhan] ASC),
    CONSTRAINT [UQ__TinNhanC__7AFA4A44954EC8FE] UNIQUE ([MaPhienChat] ASC, [MaTK_Gui] ASC, [ClientMessageId] ASC),
    CONSTRAINT [CK__TinNhanCh__LoaiT__59904A2C] CHECK ([LoaiTinNhan]='HinhAnh' OR [LoaiTinNhan]='TepTin' OR [LoaiTinNhan]='VanBan'),
    CONSTRAINT [CK__TinNhanChat__5A846E65] CHECK ([LoaiTinNhan]<>'VanBan' OR [NoiDung] IS NOT NULL AND len(ltrim(rtrim([NoiDung])))>(0)),
    CONSTRAINT [CK__TinNhanChat__5B78929E] CHECK ([NgayXem] IS NULL OR [NgayXem]>=[ThoiGianGui])
);
GO

CREATE TABLE [dbo].[TepDinhKem] (
    [MaTep] bigint IDENTITY(1,1) NOT NULL,
    [MaTK_TaiLen] int NOT NULL,
    [MaYeuCau] int NULL,
    [MaPhanHoi] int NULL,
    [MaTinNhan] bigint NULL,
    [MaBaiViet] int NULL,
    [MaFAQ] int NULL,
    [TenGoc] nvarchar(255) NOT NULL,
    [MimeType] varchar(100) NOT NULL,
    [DungLuong] bigint NOT NULL,
    [CloudinaryAssetId] varchar(255) NOT NULL,
    [CloudinaryPublicId] varchar(500) NOT NULL,
    [ResourceType] varchar(10) NOT NULL,
    [DeliveryType] varchar(20) NOT NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_TepDinhKem] PRIMARY KEY CLUSTERED ([MaTep] ASC),
    CONSTRAINT [UQ__TepDinhK__5516818FDBDA11E5] UNIQUE ([CloudinaryAssetId] ASC),
    CONSTRAINT [CK__TepDinhKe__Deliv__53D770D6] CHECK ([DeliveryType]='authenticated' OR [DeliveryType]='private' OR [DeliveryType]='upload'),
    CONSTRAINT [CK__TepDinhKe__DungL__54CB950F] CHECK ([DungLuong]>(0) AND [DungLuong]<(10485760)),
    CONSTRAINT [CK__TepDinhKe__Resou__55BFB948] CHECK ([ResourceType]='raw' OR [ResourceType]='image'),
    CONSTRAINT [CK__TepDinhKem__56B3DD81] CHECK (((((case when [MaYeuCau] IS NULL then (0) else (1) end+case when [MaPhanHoi] IS NULL then (0) else (1) end)+case when [MaTinNhan] IS NULL then (0) else (1) end)+case when [MaBaiViet] IS NULL then (0) else (1) end)+case when [MaFAQ] IS NULL then (0) else (1) end)=(1)),
    CONSTRAINT [CK__TepDinhKem__57A801BA] CHECK ([MaYeuCau] IS NULL AND [MaPhanHoi] IS NULL AND [MaTinNhan] IS NULL OR [DeliveryType]<>'upload')
);
GO

CREATE TABLE [dbo].[ThongBao] (
    [MaThongBao] bigint IDENTITY(1,1) NOT NULL,
    [MaTK_Nhan] int NOT NULL,
    [TieuDe] nvarchar(200) NOT NULL,
    [NoiDung] nvarchar(500) NOT NULL,
    [LoaiThongBao] varchar(20) NOT NULL,
    [LienKet] varchar(500) NULL,
    [DaDoc] bit NOT NULL DEFAULT ((0)),
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [KhoaChongLap] varchar(150) NOT NULL,
    CONSTRAINT [PK_ThongBao] PRIMARY KEY CLUSTERED ([MaThongBao] ASC),
    CONSTRAINT [UQ__ThongBao__E22613054369F4E6] UNIQUE ([MaTK_Nhan] ASC, [KhoaChongLap] ASC),
    CONSTRAINT [CK__ThongBao__LoaiTh__589C25F3] CHECK ([LoaiThongBao]='SLA' OR [LoaiThongBao]='HeThong' OR [LoaiThongBao]='Chatbox' OR [LoaiThongBao]='DienDan' OR [LoaiThongBao]='LichHen')
);
GO

CREATE TABLE [dbo].[NhatKyBaoMat] (
    [MaSuKien] bigint IDENTITY(1,1) NOT NULL,
    [MaTK] int NULL,
    [LoaiSuKien] varchar(50) NOT NULL,
    [ThanhCong] bit NOT NULL,
    [IPHash] binary(32) NULL,
    [UserAgentHash] binary(32) NULL,
    [ChiTiet] nvarchar(500) NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    CONSTRAINT [PK_NhatKyBaoMat] PRIMARY KEY CLUSTERED ([MaSuKien] ASC)
);
GO

CREATE TABLE [dbo].[YeuCauXacMinhOTP] (
    [MaOTP] uniqueidentifier NOT NULL DEFAULT (newid()),
    [MaTK] int NULL,
    [Email] varchar(254) NOT NULL,
    [MucDich] varchar(30) NOT NULL,
    [OTPHash] varchar(255) NOT NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [HetHan] datetime2(3) NOT NULL,
    [DaDungLuc] datetime2(3) NULL,
    [SoLanNhapSai] tinyint NOT NULL DEFAULT ((0)),
    [SoLanGui] tinyint NOT NULL DEFAULT ((1)),
    [GuiLaiSau] datetime2(3) NOT NULL,
    [IPHash] binary(32) NULL,
    [SoDienThoai] varchar(15) NULL,
    [KenhXacMinh] varchar(10) NULL DEFAULT ('EMAIL'),
    CONSTRAINT [PK_YeuCauXacMinhOTP] PRIMARY KEY CLUSTERED ([MaOTP] ASC),
    CONSTRAINT [CK__YeuCauXac__SoLan__6225902D] CHECK ([SoLanNhapSai]>=(0) AND [SoLanNhapSai]<=(5)),
    CONSTRAINT [CK__YeuCauXac__SoLan__6319B466] CHECK ([SoLanGui]>=(1) AND [SoLanGui]<=(3)),
    CONSTRAINT [CK__YeuCauXacMinhOTP__640DD89F] CHECK ([HetHan]>[NgayTao]),
    CONSTRAINT [CK__YeuCauXacMinhOTP__6501FCD8] CHECK ([GuiLaiSau]>=[NgayTao]),
    CONSTRAINT [CK__YeuCauXacMinhOTP__65F62111] CHECK ([DaDungLuc] IS NULL OR [DaDungLuc]>=[NgayTao] AND [DaDungLuc]<=[HetHan]),
    CONSTRAINT [CK_YeuCauXacMinhOTP_MucDich] CHECK ([MucDich]='DoiEmail' OR [MucDich]='DatLaiMatKhau' OR [MucDich]='XacMinhEmail' OR [MucDich]='DangKyCoVan' OR [MucDich]='DangKy')
);
GO

CREATE TABLE [dbo].[HangDoiSuKien] (
    [MaSuKien] bigint IDENTITY(1,1) NOT NULL,
    [LoaiSuKien] varchar(60) NOT NULL,
    [KhoaChongLap] varchar(150) NOT NULL,
    [Payload] nvarchar(max) NOT NULL,
    [NgayTao] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [XuLySau] datetime2(3) NOT NULL DEFAULT (dateadd(hour,(7),sysutcdatetime())),
    [TrangThai] varchar(20) NOT NULL DEFAULT ('ChoXuLy'),
    [SoLanThu] int NOT NULL DEFAULT ((0)),
    [KhoaDen] datetime2(3) NULL,
    [LoiCuoi] nvarchar(1000) NULL,
    CONSTRAINT [PK_HangDoiSuKien] PRIMARY KEY CLUSTERED ([MaSuKien] ASC),
    CONSTRAINT [UQ__HangDoiS__17A6B273337CFF14] UNIQUE ([KhoaChongLap] ASC),
    CONSTRAINT [CK__HangDoiSu__Paylo__3A179ED3] CHECK (isjson([Payload])=(1)),
    CONSTRAINT [CK__HangDoiSu__SoLan__3B0BC30C] CHECK ([SoLanThu]>=(0)),
    CONSTRAINT [CK__HangDoiSu__Trang__3BFFE745] CHECK ([TrangThai]='ThatBai' OR [TrangThai]='ThanhCong' OR [TrangThai]='DangXuLy' OR [TrangThai]='ChoXuLy')
);
GO

ALTER TABLE [dbo].[BaiVietHuongDan] ADD CONSTRAINT [FK__BaiVietHu__MaLin__7E02B4CC] FOREIGN KEY ([MaLinhVuc]) REFERENCES [dbo].[LinhVucTuVan] ([MaLinhVuc]);
ALTER TABLE [dbo].[BaiVietHuongDan] ADD CONSTRAINT [FK__BaiVietHu__MaTK___7EF6D905] FOREIGN KEY ([MaTK_Admin]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[BaiVietHuongDan] ADD CONSTRAINT [FK__BaiVietHu__MaTK___7FEAFD3E] FOREIGN KEY ([MaTK_Duyet]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[CauHinhHeThong] ADD CONSTRAINT [FK__CauHinhHe__MaTK___00DF2177] FOREIGN KEY ([MaTK_CapNhat]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[CauHoiDienDan] ADD CONSTRAINT [FK__CauHoiDie__MaYeu__01D345B0] FOREIGN KEY ([MaYeuCau]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[CauHoiFAQ] ADD CONSTRAINT [FK__CauHoiFAQ__MaLin__02C769E9] FOREIGN KEY ([MaLinhVuc]) REFERENCES [dbo].[LinhVucTuVan] ([MaLinhVuc]);
ALTER TABLE [dbo].[CauHoiFAQ] ADD CONSTRAINT [FK__CauHoiFAQ__MaTK___03BB8E22] FOREIGN KEY ([MaTK_DeXuat]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[CauHoiFAQ] ADD CONSTRAINT [FK__CauHoiFAQ__MaTK___04AFB25B] FOREIGN KEY ([MaTK_Duyet]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[CauHoiFAQ] ADD CONSTRAINT [FK__CauHoiFAQ__MaYeu__05A3D694] FOREIGN KEY ([MaYeuCauNguon]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[DanhGiaBuoiTuVan] ADD CONSTRAINT [FK__DanhGiaBuoiTuVan__0880433F] FOREIGN KEY ([MaLichHen], [MaSV]) REFERENCES [dbo].[LichHenTuVan] ([MaLichHen], [MaSV]);
ALTER TABLE [dbo].[DanhGiaYeuCau] ADD CONSTRAINT [FK__DanhGiaYeuCau__09746778] FOREIGN KEY ([MaYeuCau], [MaSV]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau], [MaSV]);
ALTER TABLE [dbo].[DonVi] ADD CONSTRAINT [FK__DonVi__MaTK_Truo__0B5CAFEA] FOREIGN KEY ([MaTK_TruongDonVi]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[KhungGioTuVan] ADD CONSTRAINT [FK__KhungGioTu__MaCV__0C50D423] FOREIGN KEY ([MaCV]) REFERENCES [dbo].[NguoiTuVan] ([MaCV]);
ALTER TABLE [dbo].[LichHenTuVan] ADD CONSTRAINT [FK__LichHenTuV__MaSV__0D44F85C] FOREIGN KEY ([MaSV]) REFERENCES [dbo].[SinhVien] ([MaSV]);
ALTER TABLE [dbo].[LichHenTuVan] ADD CONSTRAINT [FK__LichHenTuVan__0E391C95] FOREIGN KEY ([MaKhungGio], [MaCV]) REFERENCES [dbo].[KhungGioTuVan] ([MaKhungGio], [MaCV]);
ALTER TABLE [dbo].[LichSuYeuCau] ADD CONSTRAINT [FK__LichSuYeu__MaDon__0F2D40CE] FOREIGN KEY ([MaDonViCu]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[LichSuYeuCau] ADD CONSTRAINT [FK__LichSuYeu__MaDon__10216507] FOREIGN KEY ([MaDonViMoi]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[LichSuYeuCau] ADD CONSTRAINT [FK__LichSuYeu__MaTK___11158940] FOREIGN KEY ([MaTK_ThucHien]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[LichSuYeuCau] ADD CONSTRAINT [FK__LichSuYeu__MaYeu__1209AD79] FOREIGN KEY ([MaYeuCau]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[LinhVucTuVan] ADD CONSTRAINT [FK__LinhVucTu__MaDon__12FDD1B2] FOREIGN KEY ([MaDonVi]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[MauTraLoi] ADD CONSTRAINT [FK__MauTraLoi__MaDon__13F1F5EB] FOREIGN KEY ([MaDonVi]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[MauTraLoi] ADD CONSTRAINT [FK__MauTraLoi__MaTK___14E61A24] FOREIGN KEY ([MaTK_Tao]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[NguoiTuVan] ADD CONSTRAINT [FK__NguoiTuVa__MaDon__15DA3E5D] FOREIGN KEY ([MaDonVi]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[NguoiTuVan] ADD CONSTRAINT [FK__NguoiTuVan__MaTK__16CE6296] FOREIGN KEY ([MaTK]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[NhatKyBaoMat] ADD CONSTRAINT [FK__NhatKyBaoM__MaTK__17C286CF] FOREIGN KEY ([MaTK]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[PhanCongTuVan] ADD CONSTRAINT [FK__PhanCongT__MaCV___19AACF41] FOREIGN KEY ([MaCV_PhuTrach]) REFERENCES [dbo].[NguoiTuVan] ([MaCV]);
ALTER TABLE [dbo].[PhanCongTuVan] ADD CONSTRAINT [FK__PhanCongT__MaTK___1A9EF37A] FOREIGN KEY ([MaTK_ThucHien]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[PhanCongTuVan] ADD CONSTRAINT [FK__PhanCongT__MaYeu__1B9317B3] FOREIGN KEY ([MaYeuCau]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[PhanHoiTuVan] ADD CONSTRAINT [FK__PhanHoiTu__MaTK___1C873BEC] FOREIGN KEY ([MaTK_Gui]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[PhanHoiTuVan] ADD CONSTRAINT [FK__PhanHoiTu__MaYeu__1D7B6025] FOREIGN KEY ([MaYeuCau]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[PhienChat] ADD CONSTRAINT [FK__PhienChat__MaCV__1E6F845E] FOREIGN KEY ([MaCV]) REFERENCES [dbo].[NguoiTuVan] ([MaCV]);
ALTER TABLE [dbo].[PhienChat] ADD CONSTRAINT [FK__PhienChat__MaDon__1F63A897] FOREIGN KEY ([MaDonVi]) REFERENCES [dbo].[DonVi] ([MaDonVi]);
ALTER TABLE [dbo].[PhienChat] ADD CONSTRAINT [FK__PhienChat__MaSV__2057CCD0] FOREIGN KEY ([MaSV]) REFERENCES [dbo].[SinhVien] ([MaSV]);
ALTER TABLE [dbo].[SinhVien] ADD CONSTRAINT [FK__SinhVien__MaTK__22401542] FOREIGN KEY ([MaTK]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaBai__2334397B] FOREIGN KEY ([MaBaiViet]) REFERENCES [dbo].[BaiVietHuongDan] ([MaBaiViet]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaFAQ__24285DB4] FOREIGN KEY ([MaFAQ]) REFERENCES [dbo].[CauHoiFAQ] ([MaFAQ]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaPha__251C81ED] FOREIGN KEY ([MaPhanHoi]) REFERENCES [dbo].[PhanHoiTuVan] ([MaPhanHoi]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaTin__2610A626] FOREIGN KEY ([MaTinNhan]) REFERENCES [dbo].[TinNhanChat] ([MaTinNhan]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaTK___2704CA5F] FOREIGN KEY ([MaTK_TaiLen]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[TepDinhKem] ADD CONSTRAINT [FK__TepDinhKe__MaYeu__27F8EE98] FOREIGN KEY ([MaYeuCau]) REFERENCES [dbo].[YeuCauTuVan] ([MaYeuCau]);
ALTER TABLE [dbo].[ThongBao] ADD CONSTRAINT [FK__ThongBao__MaTK_N__28ED12D1] FOREIGN KEY ([MaTK_Nhan]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[TinNhanChat] ADD CONSTRAINT [FK__TinNhanCh__MaPhi__29E1370A] FOREIGN KEY ([MaPhienChat]) REFERENCES [dbo].[PhienChat] ([MaPhienChat]);
ALTER TABLE [dbo].[TinNhanChat] ADD CONSTRAINT [FK__TinNhanCh__MaTK___2AD55B43] FOREIGN KEY ([MaTK_Gui]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
ALTER TABLE [dbo].[YeuCauTuVan] ADD CONSTRAINT [FK__YeuCauTuV__MaCV___2CBDA3B5] FOREIGN KEY ([MaCV_PhuTrach]) REFERENCES [dbo].[NguoiTuVan] ([MaCV]);
ALTER TABLE [dbo].[YeuCauTuVan] ADD CONSTRAINT [FK__YeuCauTuVa__MaSV__2DB1C7EE] FOREIGN KEY ([MaSV]) REFERENCES [dbo].[SinhVien] ([MaSV]);
ALTER TABLE [dbo].[YeuCauTuVan] ADD CONSTRAINT [FK__YeuCauTuVan__2EA5EC27] FOREIGN KEY ([MaLinhVuc], [MaDonVi]) REFERENCES [dbo].[LinhVucTuVan] ([MaLinhVuc], [MaDonVi]);
ALTER TABLE [dbo].[YeuCauXacMinhOTP] ADD CONSTRAINT [FK__YeuCauXacM__MaTK__2F9A1060] FOREIGN KEY ([MaTK]) REFERENCES [dbo].[TaiKhoan] ([MaTK]);
GO

CREATE NONCLUSTERED INDEX [IX_BaiViet_CongKhai] ON [dbo].[BaiVietHuongDan] ([TrangThai] ASC, [MaLinhVuc] ASC, [NgayDang] ASC);
CREATE NONCLUSTERED INDEX [IX_FAQ_CongKhai] ON [dbo].[CauHoiFAQ] ([TrangThai] ASC, [MaLinhVuc] ASC);
CREATE NONCLUSTERED INDEX [IX_Outbox_ChoXuLy] ON [dbo].[HangDoiSuKien] ([TrangThai] ASC, [XuLySau] ASC);
CREATE NONCLUSTERED INDEX [IX_LichHen_CoVan] ON [dbo].[LichHenTuVan] ([MaCV] ASC, [TrangThai] ASC);
CREATE NONCLUSTERED INDEX [IX_LichHen_SinhVien] ON [dbo].[LichHenTuVan] ([MaSV] ASC, [TrangThai] ASC);
CREATE UNIQUE NONCLUSTERED INDEX [UX_LichHen_GiuCho] ON [dbo].[LichHenTuVan] ([MaKhungGio] ASC) WHERE ([TrangThai] IN ('ChoDuyet', 'DaXacNhan', 'HoanThanh'));
CREATE NONCLUSTERED INDEX [IX_LichSu_YeuCau] ON [dbo].[LichSuYeuCau] ([MaYeuCau] ASC, [NgayTao] ASC);
CREATE NONCLUSTERED INDEX [IX_NhatKyBaoMat_IP] ON [dbo].[NhatKyBaoMat] ([IPHash] ASC, [NgayTao] ASC) WHERE ([IPHash] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_NhatKyBaoMat_TaiKhoan] ON [dbo].[NhatKyBaoMat] ([MaTK] ASC, [NgayTao] ASC);
CREATE UNIQUE NONCLUSTERED INDEX [UX_PhanCong_HienTai] ON [dbo].[PhanCongTuVan] ([MaYeuCau] ASC) WHERE ([NgayKetThuc] IS NULL);
CREATE NONCLUSTERED INDEX [IX_PhanHoi_YeuCau] ON [dbo].[PhanHoiTuVan] ([MaYeuCau] ASC, [NgayTraLoi] ASC, [MaPhanHoi] ASC);
CREATE NONCLUSTERED INDEX [IX_Chat_HangDoi] ON [dbo].[PhienChat] ([MaDonVi] ASC, [TrangThai] ASC, [ThoiGianBatDau] ASC);
CREATE NONCLUSTERED INDEX [IX_Chat_SinhVien] ON [dbo].[PhienChat] ([MaSV] ASC, [ThoiGianBatDau] ASC);
CREATE NONCLUSTERED INDEX [IX_Tep_BaiViet] ON [dbo].[TepDinhKem] ([MaBaiViet] ASC) WHERE ([MaBaiViet] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_Tep_FAQ] ON [dbo].[TepDinhKem] ([MaFAQ] ASC) WHERE ([MaFAQ] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_Tep_PhanHoi] ON [dbo].[TepDinhKem] ([MaPhanHoi] ASC) WHERE ([MaPhanHoi] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_Tep_TinNhan] ON [dbo].[TepDinhKem] ([MaTinNhan] ASC) WHERE ([MaTinNhan] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_Tep_YeuCau] ON [dbo].[TepDinhKem] ([MaYeuCau] ASC) WHERE ([MaYeuCau] IS NOT NULL);
CREATE NONCLUSTERED INDEX [IX_ThongBao_ChuaDoc] ON [dbo].[ThongBao] ([MaTK_Nhan] ASC, [NgayTao] ASC) WHERE ([DaDoc]=(0));
CREATE NONCLUSTERED INDEX [IX_TinNhan_LichSu] ON [dbo].[TinNhanChat] ([MaPhienChat] ASC, [MaTinNhan] ASC);
CREATE NONCLUSTERED INDEX [IX_YeuCau_HangDoi] ON [dbo].[YeuCauTuVan] ([MaDonVi] ASC, [TrangThai] ASC, [NgayTao] ASC) INCLUDE ([MaCV_PhuTrach], [HanSLA]);
CREATE NONCLUSTERED INDEX [IX_YeuCau_SinhVien] ON [dbo].[YeuCauTuVan] ([MaSV] ASC, [NgayTao] ASC);
CREATE NONCLUSTERED INDEX [IX_YeuCau_SLA] ON [dbo].[YeuCauTuVan] ([TrangThai] ASC, [HanSLA] ASC) INCLUDE ([HanTiepNhan], [NgayTiepNhan], [NgayPhanHoiDau]);
CREATE NONCLUSTERED INDEX [IX_OTP_TraCuu] ON [dbo].[YeuCauXacMinhOTP] ([Email] ASC, [MucDich] ASC, [NgayTao] ASC) INCLUDE ([HetHan], [DaDungLuc], [SoLanNhapSai], [GuiLaiSau]);
GO

CREATE VIEW [dbo].[vw_BaiVietCongKhai] AS
 SELECT MaBaiViet,TieuDe,TomTat,NoiDung,TuKhoa,MaLinhVuc,LuotXem,NgayDang
 FROM dbo.BaiVietHuongDan WHERE TrangThai='XuatBan';
GO

CREATE VIEW [dbo].[vw_FAQCongKhai] AS
 SELECT MaFAQ,CauHoi,CauTraLoi,MaLinhVuc,TuKhoa,LuotXem
 FROM dbo.CauHoiFAQ WHERE TrangThai='HienThi';
GO

CREATE   VIEW dbo.vw_ThongKeYeuCauTheoNgay AS
 SELECT CAST(NgayTao AS date) AS NgayVietNam,MaDonVi,MaLinhVuc,COUNT_BIG(*) AS TongYeuCau,
 SUM(CAST(CASE WHEN TrangThai IN ('DaPhanHoi','Dong') THEN 1 ELSE 0 END AS bigint)) AS DaHoanTat,
 CAST(100.0*SUM(CASE WHEN TrangThai IN ('DaPhanHoi','Dong') THEN 1 ELSE 0 END)/NULLIF(COUNT_BIG(*),0) AS decimal(6,2)) AS TyLeHoanTat
 FROM dbo.YeuCauTuVan GROUP BY CAST(NgayTao AS date),MaDonVi,MaLinhVuc;
GO
