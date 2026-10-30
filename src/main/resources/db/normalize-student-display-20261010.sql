-- One-time display normalization of the explicitly seeded batch; no schema or existing-user changes.
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit=0;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @batch TABLE(accountId int PRIMARY KEY,studentId int,n int,faculty int,newMssv varchar(20));
 INSERT @batch
 SELECT t.MaTK,s.MaSV,CONVERT(int,RIGHT(t.TenDangNhap,3)),DENSE_RANK() OVER(ORDER BY s.Khoa),
  CONVERT(varchar(20),24119000+CONVERT(int,RIGHT(t.TenDangNhap,3)))
 FROM dbo.TaiKhoan t JOIN dbo.SinhVien s ON s.MaTK=t.MaTK WHERE t.TenDangNhap LIKE 'demo261010[_]%';
 IF NOT EXISTS(SELECT 1 FROM @batch)
 BEGIN
  SELECT 'No old-format batch remains; no changes made' Result;
  ROLLBACK; RETURN;
 END;
 IF (SELECT COUNT(*) FROM @batch)<>80 THROW 51010,'Expected exactly 80 seeded students.',1;
 IF EXISTS(SELECT 1 FROM @batch b JOIN dbo.SinhVien s ON s.MSSV=b.newMssv AND s.MaSV<>b.studentId)
  OR EXISTS(SELECT 1 FROM @batch b JOIN dbo.TaiKhoan t ON (t.TenDangNhap=CONCAT('sv',b.newMssv) OR t.Email=CONCAT('sv',b.newMssv,'@example.invalid')) AND t.MaTK<>b.accountId)
  THROW 51011,'Student number, username or email collision; no changes applied.',1;
 UPDATE s SET MSSV=b.newMssv,Lop=CONCAT('24',FORMAT(100+b.faculty,'000'),'CLC',1+(b.n-1)/16),KhoaHoc='2024-2028'
 FROM dbo.SinhVien s JOIN @batch b ON b.studentId=s.MaSV;
 UPDATE t SET TenDangNhap=CONCAT('sv',b.newMssv),Email=CONCAT('sv',b.newMssv,'@example.invalid'),
 HoTen=CONCAT(CASE (b.n-1)/8 WHEN 0 THEN N'Nguyễn' WHEN 1 THEN N'Trần' WHEN 2 THEN N'Lê' WHEN 3 THEN N'Phạm' WHEN 4 THEN N'Võ' WHEN 5 THEN N'Đặng' WHEN 6 THEN N'Bùi' WHEN 7 THEN N'Đỗ' WHEN 8 THEN N'Huỳnh' ELSE N'Phan' END,N' ',
 CASE (b.n-1)%8 WHEN 0 THEN N'Minh Anh' WHEN 1 THEN N'Hoàng Nam' WHEN 2 THEN N'Ngọc Linh' WHEN 3 THEN N'Gia Huy' WHEN 4 THEN N'Thanh Hà' WHEN 5 THEN N'Tuấn Kiệt' WHEN 6 THEN N'Mai Phương' ELSE N'Khánh An' END)
 FROM dbo.TaiKhoan t JOIN @batch b ON b.accountId=t.MaTK;
 UPDATE y SET TieuDe=REPLACE(LEFT(y.TieuDe,CASE WHEN CHARINDEX(N' #',y.TieuDe)>0 THEN CHARINDEX(N' #',y.TieuDe)-1 ELSE LEN(y.TieuDe) END),N'[Mẫu] ',N''),
 NoiDung=REPLACE(y.NoiDung,N'Dữ liệu minh họa cho báo cáo QAUTE. ',N''),KetLuan=REPLACE(y.KetLuan,N'[Mẫu] ',N'')
 FROM dbo.YeuCauTuVan y JOIN @batch b ON b.studentId=y.MaSV;
 UPDATE p SET NoiDungTraLoi=REPLACE(REPLACE(p.NoiDungTraLoi,N'[Mẫu] ',N''),N' Nội dung này chỉ minh họa luồng tư vấn, không phải thông báo chính thức.',N'')
 FROM dbo.PhanHoiTuVan p JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=p.MaYeuCau JOIN @batch b ON b.studentId=y.MaSV;
 UPDATE p SET GhiChu=N'Phân công cố vấn phụ trách theo lĩnh vực tư vấn'
 FROM dbo.PhanCongTuVan p JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=p.MaYeuCau JOIN @batch b ON b.studentId=y.MaSV;
 UPDATE h SET LyDo=REPLACE(REPLACE(h.LyDo,N'[Mẫu] ',N''),N' minh họa',N'')
 FROM dbo.LichSuYeuCau h JOIN dbo.YeuCauTuVan y ON y.MaYeuCau=h.MaYeuCau JOIN @batch b ON b.studentId=y.MaSV;
 UPDATE h SET ChuDe=REPLACE(LEFT(h.ChuDe,CASE WHEN CHARINDEX(N' #',h.ChuDe)>0 THEN CHARINDEX(N' #',h.ChuDe)-1 ELSE LEN(h.ChuDe) END),N'[Mẫu] ',N''),
 NoiDungHen=REPLACE(h.NoiDungHen,N'[Mẫu] ',N''),LyDo=REPLACE(h.LyDo,N'[Mẫu] ',N''),LinkHop_Phong=REPLACE(h.LinkHop_Phong,N'[Mẫu] ',N''),KetLuan=REPLACE(h.KetLuan,N'[Mẫu] ',N'')
 FROM dbo.LichHenTuVan h JOIN @batch b ON b.studentId=h.MaSV;
 UPDATE k SET DiaDiem_Link=REPLACE(k.DiaDiem_Link,N'[Mẫu] ',N'')
 FROM dbo.KhungGioTuVan k JOIN dbo.LichHenTuVan h ON h.MaKhungGio=k.MaKhungGio JOIN @batch b ON b.studentId=h.MaSV;
 UPDATE d SET NhanXet=REPLACE(d.NhanXet,N'[Mẫu] ',N'') FROM dbo.DanhGiaYeuCau d JOIN @batch b ON b.studentId=d.MaSV;
 UPDATE d SET NhanXet=REPLACE(d.NhanXet,N'[Mẫu] ',N'') FROM dbo.DanhGiaBuoiTuVan d JOIN @batch b ON b.studentId=d.MaSV;
 SELECT COUNT(*) StudentsUpdated FROM @batch;
 SELECT TOP 5 s.MSSV,s.Lop,s.KhoaHoc,t.HoTen FROM @batch b JOIN dbo.SinhVien s ON s.MaSV=b.studentId JOIN dbo.TaiKhoan t ON t.MaTK=b.accountId ORDER BY b.n;
 IF @Apply=1 COMMIT; ELSE ROLLBACK;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
