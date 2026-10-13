-- Optional demo batch. Never run automatically on application startup.
-- After a fresh import, apply normalize-student-display-20261010.sql for natural display formats.
-- Both original and normalized account names are recognized to prevent re-insertion.
-- Azure QAUTE only; @Apply=0 validates the entire batch then rolls it back.
-- All sample accounts are locked, use non-deliverable email and have no usable password.
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit=0;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @lock int;
 EXEC @lock=sys.sp_getapplock @Resource='QAUTE-demo-20261010',@LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000;
 IF @lock<0 THROW 51000,'Cannot acquire demo batch lock.',1;
 IF EXISTS(SELECT 1 FROM dbo.TaiKhoan WHERE (TenDangNhap LIKE 'demo261010[_]%' OR (TenDangNhap BETWEEN 'sv24119001' AND 'sv24119080' AND LEN(TenDangNhap)=10)))
 BEGIN
  IF (SELECT COUNT(*) FROM dbo.TaiKhoan WHERE (TenDangNhap LIKE 'demo261010[_]%' OR (TenDangNhap BETWEEN 'sv24119001' AND 'sv24119080' AND LEN(TenDangNhap)=10)))<>80
   OR (SELECT COUNT(*) FROM dbo.YeuCauTuVan y JOIN dbo.SinhVien s ON s.MaSV=y.MaSV JOIN dbo.TaiKhoan t ON t.MaTK=s.MaTK WHERE (t.TenDangNhap LIKE 'demo261010[_]%' OR (t.TenDangNhap BETWEEN 'sv24119001' AND 'sv24119080' AND LEN(t.TenDangNhap)=10)))<>240
   OR (SELECT COUNT(*) FROM dbo.LichHenTuVan h JOIN dbo.SinhVien s ON s.MaSV=h.MaSV JOIN dbo.TaiKhoan t ON t.MaTK=s.MaTK WHERE (t.TenDangNhap LIKE 'demo261010[_]%' OR (t.TenDangNhap BETWEEN 'sv24119001' AND 'sv24119080' AND LEN(t.TenDangNhap)=10)))<>64
   THROW 51001,'Partial demo batch exists; inspect before retrying.',1;
  SELECT 'Already present; no changes made' AS Result;
  ROLLBACK; RETURN;
 END;
 DECLARE @now datetime2(3)=DATEADD(HOUR,7,SYSUTCDATETIME());
 DECLARE @units TABLE(n int PRIMARY KEY,unitId int,fieldId int,advisorId int,advisorAccount int,unitName nvarchar(150),fieldName nvarchar(100));
 INSERT @units
 SELECT ROW_NUMBER() OVER(ORDER BY d.MaDonVi),d.MaDonVi,f.MaLinhVuc,a.MaCV,a.MaTK,d.TenDonVi,f.TenLinhVuc
 FROM dbo.DonVi d
 CROSS APPLY(SELECT TOP 1 l.MaLinhVuc,l.TenLinhVuc FROM dbo.LinhVucTuVan l WHERE l.MaDonVi=d.MaDonVi AND l.TrangThai='HoatDong' AND l.NhayCam=0 ORDER BY l.MaLinhVuc) f
 CROSS APPLY(SELECT TOP 1 c.MaCV,c.MaTK FROM dbo.NguoiTuVan c JOIN dbo.TaiKhoan t ON t.MaTK=c.MaTK WHERE c.MaDonVi=d.MaDonVi AND c.NhanTuVan=1 AND t.VaiTro='CoVan' AND t.TrangThai='HoatDong' ORDER BY c.MaCV) a
 WHERE d.HoatDong=1;
 DECLARE @unitCount int=(SELECT COUNT(*) FROM @units);
 IF @unitCount<10 THROW 51002,'Expected at least ten configured advisor units.',1;
 DECLARE @students TABLE(n int PRIMARY KEY,studentId int,accountId int);
 DECLARE @i int=1,@account int,@student int,@unit int,@field int,@advisor int,@advisorAccount int,
 @unitName nvarchar(150),@fieldName nvarchar(100),@created datetime2(3),@status varchar(20),@id int,@slot int,@appointment int;
 WHILE @i<=80
 BEGIN
  SELECT @unitName=unitName,@fieldName=fieldName FROM @units WHERE n=1+(@i-1)%@unitCount;
  INSERT dbo.TaiKhoan(TenDangNhap,MatKhau,HoTen,Email,VaiTro,TrangThai,BatBuocDoiMatKhau,NgayTao,NgayCapNhat)
  VALUES(CONCAT('demo261010_',FORMAT(@i,'000')),CONVERT(varchar(64),CRYPT_GEN_RANDOM(32),2),
   CONCAT(N'[Mẫu] ',CASE @i%8 WHEN 0 THEN N'Nguyễn Minh Anh' WHEN 1 THEN N'Trần Hoàng Nam' WHEN 2 THEN N'Lê Ngọc Linh' WHEN 3 THEN N'Phạm Gia Huy' WHEN 4 THEN N'Võ Thanh Hà' WHEN 5 THEN N'Đặng Tuấn Kiệt' WHEN 6 THEN N'Bùi Mai Phương' ELSE N'Đỗ Khánh An' END,N' ',FORMAT(@i,'000')),
   CONCAT('demo261010_',FORMAT(@i,'000'),'@example.invalid'),'SinhVien','Khoa',1,DATEADD(day,-365,@now),@now);
  SET @account=SCOPE_IDENTITY();
  INSERT dbo.SinhVien(MSSV,Khoa,Nganh,Lop,KhoaHoc,MaTK)
  VALUES(CONCAT('DEMO261010',FORMAT(@i,'000')),LEFT(@unitName,100),LEFT(@fieldName,100),CONCAT('DEMO',2022+@i%4,'-',1+@i%5),CONCAT(2022+@i%4,'-',2026+@i%4),@account);
  SET @student=SCOPE_IDENTITY();
  INSERT @students VALUES(@i,@student,@account);
  SET @i+=1;
 END;
 SET @i=1;
 WHILE @i<=240
 BEGIN
  SELECT @student=studentId,@account=accountId FROM @students WHERE n=1+(@i-1)%80;
  -- Uneven distribution makes differences between units visible.
  SELECT @unit=unitId,@field=fieldId,@advisor=advisorId,@advisorAccount=advisorAccount,@unitName=unitName,@fieldName=fieldName
   FROM @units WHERE n=1+((@i*@i+@i/7)%@unitCount);
  SET @status=CASE WHEN @i%5=0 THEN 'DaPhanHoi' ELSE 'Dong' END;
  SET @created=CASE WHEN @status='DaPhanHoi' THEN DATEADD(hour,-24-@i%48,@now) ELSE DATEADD(day,-7-@i%170,@now) END;
  INSERT dbo.YeuCauTuVan(MaSV,MaLinhVuc,MaDonVi,MaCV_PhuTrach,TieuDe,NoiDung,TrangThai,NgayTao,NgayCapNhat,
   HanTiepNhan,HanSLA,NgayTiepNhan,NgayPhanHoiDau,NgayPhanHoiCuoi,HanTuDongDong,NgayDong,KetLuan)
  VALUES(@student,@field,@unit,@advisor,
   CONCAT(N'[Mẫu] ',CASE @i%6 WHEN 0 THEN N'Lập kế hoạch học tập' WHEN 1 THEN N'Chuẩn bị hồ sơ thực tập' WHEN 2 THEN N'Định hướng kỹ năng nghề nghiệp' WHEN 3 THEN N'Tham gia hoạt động học thuật' WHEN 4 THEN N'Tìm hiểu điều kiện tốt nghiệp' ELSE N'Chuẩn bị đề tài nghiên cứu' END,N' — ',@unitName,N' #',@i),
   CONCAT(N'Dữ liệu minh họa cho báo cáo QAUTE. Em muốn được hướng dẫn về ',@fieldName,N'. Em đang sắp xếp kế hoạch học kỳ và cần biết các bước chuẩn bị, hồ sơ cần thiết cùng đầu mối hỗ trợ. Nhờ cố vấn tư vấn cách xây dựng kế hoạch phù hợp.'),
   @status,@created,CASE WHEN @status='Dong' THEN DATEADD(day,3,@created) ELSE DATEADD(hour,8,@created) END,DATEADD(hour,24,@created),DATEADD(hour,48,@created),DATEADD(hour,2,@created),DATEADD(hour,8,@created),DATEADD(hour,8,@created),
   CASE WHEN @status='DaPhanHoi' THEN DATEADD(day,7,DATEADD(hour,12,@created)) END,
   CASE WHEN @status='Dong' THEN DATEADD(day,3,@created) END,
   CASE WHEN @status='Dong' THEN N'[Mẫu] Đã trao đổi kế hoạch học tập và thống nhất các bước tiếp theo.' END);
  SET @id=SCOPE_IDENTITY();
  INSERT dbo.CauHoiDienDan(MaYeuCau,HienThi,LuotXem) VALUES(@id,1,0);
  INSERT dbo.PhanCongTuVan(MaYeuCau,MaTK_ThucHien,MaCV_PhuTrach,NgayPhanCong,NgayKetThuc,GhiChu)
   VALUES(@id,@advisorAccount,@advisor,DATEADD(hour,2,@created),CASE WHEN @status='Dong' THEN DATEADD(day,3,@created) END,N'[Mẫu] Phân công minh họa');
  INSERT dbo.PhanHoiTuVan(MaYeuCau,MaTK_Gui,LoaiPhanHoi,NoiDungTraLoi,NgayTraLoi)
   VALUES(@id,@advisorAccount,'TraLoi',CONCAT(N'[Mẫu] Em hãy rà soát tiến độ học tập, liệt kê các mục tiêu ưu tiên và chuẩn bị hồ sơ liên quan đến ',@fieldName,N'. Sau đó trao đổi với cố vấn của khoa để thống nhất kế hoạch từng tuần. Nội dung này chỉ minh họa luồng tư vấn, không phải thông báo chính thức.'),DATEADD(hour,8,@created));
  INSERT dbo.LichSuYeuCau(MaYeuCau,MaTK_ThucHien,HanhDong,TrangThaiMoi,MaDonViMoi,LyDo,NgayTao)
   VALUES(@id,@account,'TaoMoi','ChoTiepNhan',@unit,N'[Mẫu] Tạo câu hỏi minh họa',@created),
   (@id,@advisorAccount,'PhanHoi','DaPhanHoi',@unit,N'[Mẫu] Cố vấn trả lời',DATEADD(hour,8,@created));
  IF @status='Dong'
  BEGIN
   INSERT dbo.LichSuYeuCau(MaYeuCau,MaTK_ThucHien,HanhDong,TrangThaiCu,TrangThaiMoi,LyDo,NgayTao)
    VALUES(@id,@account,'Dong','DaPhanHoi','Dong',N'[Mẫu] Đã hoàn tất trao đổi',DATEADD(day,3,@created));
   INSERT dbo.DanhGiaYeuCau(MaYeuCau,MaSV,SoSao,NhanXet,NgayDanhGia)
    VALUES(@id,@student,3+@i%3,N'[Mẫu] Hướng dẫn rõ ràng, giúp em chủ động lập kế hoạch.',DATEADD(day,4,@created));
  END;
  SET @i+=1;
 END;
 SET @i=1;
 WHILE @i<=64
 BEGIN
  SELECT @student=studentId FROM @students WHERE n=@i;
  SELECT @advisor=advisorId FROM @units WHERE n=1+(@i-1)%@unitCount;
  SET @created=DATEADD(minute,13*60+17,CAST(CAST(DATEADD(day,-10-@i,@now) AS date) AS datetime2));
  IF EXISTS(SELECT 1 FROM dbo.KhungGioTuVan WHERE MaCV=@advisor AND BatDau=@created)
   THROW 51003,'Appointment slot collision; no demo data applied.',1;
  INSERT dbo.KhungGioTuVan(MaCV,BatDau,KetThuc,HinhThuc,DiaDiem_Link,TrangThai)
   VALUES(@advisor,@created,DATEADD(minute,60,@created),'TrucTiep',N'[Mẫu] Phòng tư vấn khoa','Khoa');
  SET @slot=SCOPE_IDENTITY();
  SET @status=CASE @i%8 WHEN 0 THEN 'Huy' WHEN 1 THEN 'TuChoi' ELSE 'HoanThanh' END;
  INSERT dbo.LichHenTuVan(MaSV,MaCV,MaKhungGio,ChuDe,NoiDungHen,TrangThai,LyDo,LinkHop_Phong,KetLuan,NgayTao,HanDuyet,NgayDuyet,NgayHoanThanh)
   VALUES(@student,@advisor,@slot,CONCAT(N'[Mẫu] Tư vấn kế hoạch học tập #',@i),N'[Mẫu] Trao đổi mục tiêu học kỳ, chuẩn bị thực tập và kế hoạch rèn luyện kỹ năng.',@status,
    CASE WHEN @status IN ('Huy','TuChoi') THEN N'[Mẫu] Lịch học thay đổi, sinh viên sẽ chọn buổi khác.' END,N'[Mẫu] Phòng tư vấn khoa',
    CASE WHEN @status='HoanThanh' THEN N'[Mẫu] Thống nhất mục tiêu và kế hoạch theo dõi tiến độ học tập.' END,
    DATEADD(day,-3,@created),DATEADD(day,-2,@created),DATEADD(day,-2,@created),CASE WHEN @status='HoanThanh' THEN DATEADD(minute,60,@created) END);
  SET @appointment=SCOPE_IDENTITY();
  IF @status='HoanThanh'
   INSERT dbo.DanhGiaBuoiTuVan(MaLichHen,MaSV,SoSao,NhanXet,NgayDanhGia)
    VALUES(@appointment,@student,3+@i%3,N'[Mẫu] Buổi tư vấn giúp làm rõ mục tiêu học kỳ.',DATEADD(hour,2,@created));
  SET @i+=1;
 END;
 SELECT @unitCount Units,80 StudentsAdded,240 ForumQuestionsAdded,240 RepliesAdded,64 AppointmentsAdded;
 SELECT d.TenDonVi,COUNT(*) SampleQuestions FROM dbo.YeuCauTuVan y JOIN dbo.DonVi d ON d.MaDonVi=y.MaDonVi JOIN @students s ON s.studentId=y.MaSV GROUP BY d.TenDonVi ORDER BY COUNT(*) DESC;
 IF @Apply=1 COMMIT; ELSE ROLLBACK;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
