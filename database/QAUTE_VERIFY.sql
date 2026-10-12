-- Database integration checks. Every fixture transaction is rolled back.
-- Identity counters can advance, but no test accounts or transactions remain.
USE QAUTE;
GO
SET NOCOUNT ON;
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
CREATE TABLE #Results (TestName nvarchar(120), Result varchar(4), ActualError int, Detail nvarchar(2000));
GO
CREATE PROCEDURE #RunCase @Name nvarchar(120), @Sql nvarchar(max), @ExpectedError int AS
BEGIN
 SET NOCOUNT ON;
 BEGIN TRY
  BEGIN TRANSACTION;
  EXEC sys.sp_executesql @Sql;
  ROLLBACK;
  INSERT #Results VALUES(@Name,CASE WHEN @ExpectedError=0 THEN 'PASS' ELSE 'FAIL' END,0,N'No error');
 END TRY
 BEGIN CATCH
  DECLARE @Err int=ERROR_NUMBER(), @Msg nvarchar(2000)=ERROR_MESSAGE();
  IF @@TRANCOUNT>0 ROLLBACK;
  INSERT #Results VALUES(@Name,CASE WHEN @Err=@ExpectedError THEN 'PASS' ELSE 'FAIL' END,@Err,@Msg);
 END CATCH;
END;
GO

EXEC #RunCase N'Valid ticket booking chat', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.TinNhanChat(MaPhienChat,MaTK_Gui,ClientMessageId,NoiDung) VALUES (@chat,@u1,NEWID(),N''Hello'');', 0;
GO

EXEC #RunCase N'Duplicate occupied slot', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet) VALUES (@s2,@cv,@slot,N''Test'',N''Test'',DATEADD(DAY,1,SYSUTCDATETIME()));', 2601;
GO

EXEC #RunCase N'Cancelled slot can be booked again', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

UPDATE dbo.LichHenTuVan SET TrangThai=''Huy'',LyDo=N''Test'' WHERE MaLichHen=@hen; INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet) VALUES (@s2,@cv,@slot,N''Test'',N''Test'',DATEADD(DAY,1,SYSUTCDATETIME()));', 0;
GO

EXEC #RunCase N'Rating before completion blocked', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao) VALUES (@hen,@s1,5);', 51001;
GO

EXEC #RunCase N'Wrong rating owner blocked', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao) VALUES (@hen,@s2,5);', 547;
GO

EXEC #RunCase N'Invalid star value blocked', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao) VALUES (@hen,@s1,6);', 547;
GO

EXEC #RunCase N'Completed appointment can be rated', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

UPDATE dbo.LichHenTuVan SET TrangThai=''HoanThanh'',LinkHop_Phong=N''P1'',KetLuan=N''Xong'',NgayHoanThanh=SYSUTCDATETIME() WHERE MaLichHen=@hen; INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao) VALUES (@hen,@s1,5);', 0;
GO

EXEC #RunCase N'Rating cannot be modified', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

UPDATE dbo.LichHenTuVan SET TrangThai=''HoanThanh'',LinkHop_Phong=N''P1'',KetLuan=N''Xong'',NgayHoanThanh=SYSUTCDATETIME() WHERE MaLichHen=@hen; INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao) VALUES (@hen,@s1,5); UPDATE dbo.DanhGiaBuoiTuVan SET SoSao=4 WHERE MaLichHen=@hen;', 51002;
GO

EXEC #RunCase N'Unanswered ticket cannot be rated', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.DanhGiaYeuCau(MaYeuCau,MaSV,SoSao) VALUES (@yc,@s1,5);', 51003;
GO

EXEC #RunCase N'Outsider cannot send chat', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.TinNhanChat(MaPhienChat,MaTK_Gui,ClientMessageId,NoiDung) VALUES (@chat,@u2,NEWID(),N''Attack'');', 51005;
GO

EXEC #RunCase N'Retry chat id is unique', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

DECLARE @id uniqueidentifier=NEWID(); INSERT dbo.TinNhanChat(MaPhienChat,MaTK_Gui,ClientMessageId,NoiDung) VALUES (@chat,@u1,@id,N''Hello''); INSERT dbo.TinNhanChat(MaPhienChat,MaTK_Gui,ClientMessageId,NoiDung) VALUES (@chat,@u1,@id,N''Hello'');', 2627;
GO

EXEC #RunCase N'FAQ needs approval before public', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.CauHoiFAQ(CauHoi,CauTraLoi,MaLinhVuc,MaTK_DeXuat,TrangThai) VALUES(N''Test'',N''Test'',@lv,@uc,''HienThi'');', 547;
GO

EXEC #RunCase N'Wrong department field blocked', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

UPDATE dbo.YeuCauTuVan SET MaDonVi=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CTSV'') WHERE MaYeuCau=@yc;', 547;
GO

EXEC #RunCase N'Ticket file cannot be public', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.TepDinhKem(MaTK_TaiLen,MaYeuCau,TenGoc,MimeType,DungLuong,CloudinaryAssetId,CloudinaryPublicId,ResourceType,DeliveryType) VALUES(@u1,@yc,N''test.pdf'',''application/pdf'',100,''test-id'',''test-public'',''raw'',''upload'');', 547;
GO

EXEC #RunCase N'File must have an owner', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.TepDinhKem(MaTK_TaiLen,MaYeuCau,MaPhanHoi,MaFAQ,TenGoc,MimeType,DungLuong,CloudinaryAssetId,CloudinaryPublicId,ResourceType,DeliveryType) VALUES(@u1,NULL,NULL,NULL,N''test.pdf'',''application/pdf'',100,''test-id'',''test-public'',''raw'',''authenticated'');', 547;
GO

EXEC #RunCase N'Oversize file blocked', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.TepDinhKem(MaTK_TaiLen,MaYeuCau,TenGoc,MimeType,DungLuong,CloudinaryAssetId,CloudinaryPublicId,ResourceType,DeliveryType) VALUES(@u1,@yc,N''test.pdf'',''application/pdf'',10485760,''test-id'',''test-public'',''raw'',''authenticated'');', 547;
GO

EXEC #RunCase N'One current assignment only', N'
INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro) VALUES
 (''__qa_sv1'',''TEST_ONLY_ROLLED_BACK'',N''Test SV1'',''__qa_sv1@example.invalid'',''SinhVien''),
 (''__qa_sv2'',''TEST_ONLY_ROLLED_BACK'',N''Test SV2'',''__qa_sv2@example.invalid'',''SinhVien''),
 (''__qa_cv'',''TEST_ONLY_ROLLED_BACK'',N''Test CV'',''__qa_cv@example.invalid'',''CoVan'');
DECLARE @u1 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv1'');
DECLARE @u2 int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_sv2'');
DECLARE @uc int=(SELECT MaTK FROM dbo.TaiKhoan WHERE TenDangNhap=''__qa_cv'');
DECLARE @dv int=(SELECT MaDonVi FROM dbo.DonVi WHERE MaDonViCode=''CNTT'');
DECLARE @lv int=(SELECT MaLinhVuc FROM dbo.LinhVucTuVan WHERE MaLinhVucCode=''NGHENGHIEP'');
INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK) VALUES
 (''__qa_sv1'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u1),
 (''__qa_sv2'',N''Khoa test'',N''Ngành test'',''TEST'',''TEST'',@u2);
DECLARE @s1 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u1), @s2 int=(SELECT MaSV FROM dbo.SinhVien WHERE MaTK=@u2);
INSERT dbo.NguoiTuVan(MaCanBo,ChucVu,ChuyenMon,MaDonVi,MaTK) VALUES (''__qa_cv'',N''Test'',N''Test'',@dv,@uc);
DECLARE @cv int=SCOPE_IDENTITY();
INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc) VALUES (@cv,''20300101 01:00:00'',''20300101 02:00:00'',''TrucTiep'');
DECLARE @slot int=SCOPE_IDENTITY();
INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,HanDuyet)
 VALUES (@s1,@cv,@slot,N''Test'',N''Nội dung test'',DATEADD(DAY,1,SYSUTCDATETIME()));
DECLARE @hen int=SCOPE_IDENTITY();
INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,TieuDe,NoiDung,HanTiepNhan,HanSLA)
 VALUES (@s1,@lv,@dv,N''Câu hỏi kiểm thử'',N''Nội dung câu hỏi kiểm thử trên hai mươi ký tự.'',DATEADD(DAY,1,SYSUTCDATETIME()),DATEADD(DAY,2,SYSUTCDATETIME()));
DECLARE @yc int=SCOPE_IDENTITY();
INSERT dbo.PhienChat(MaSV,MaDonVi,MaCV,TrangThai) VALUES (@s1,@dv,@cv,''DangHoatDong'');
DECLARE @chat int=SCOPE_IDENTITY();

INSERT dbo.PhanCongTuVan(MaYeuCau,MaTK_ThucHien,MaCV_PhuTrach) VALUES(@yc,@uc,@cv); INSERT dbo.PhanCongTuVan(MaYeuCau,MaTK_ThucHien,MaCV_PhuTrach) VALUES(@yc,@uc,@cv);', 2601;
GO


SELECT TestName,Result,ActualError FROM #Results;
IF EXISTS(SELECT 1 FROM #Results WHERE Result='FAIL')
BEGIN
 SELECT * FROM #Results WHERE Result='FAIL';
 THROW 51999, 'Database verification failed.',1;
END;
IF EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE TenDangNhap LIKE '__qa[_]%')
 THROW 51998, 'Test fixture unexpectedly remains.',1;
PRINT 'ALL 17 DATABASE TESTS PASSED; NO TEST ACCOUNTS REMAIN.';
DBCC CHECKCONSTRAINTS WITH ALL_CONSTRAINTS;
DBCC CHECKDB(N'QAUTE') WITH NO_INFOMSGS;
GO
