-- Replace placeholder names only; retain login, password, IDs and consultation history.
UPDATE t SET HoTen = CASE UPPER(d.MaDonViCode)
 WHEN 'CKCTM' THEN N'ThS. Nguyễn Đức Thành'
 WHEN 'CNHHTP' THEN N'ThS. Phạm Thị Ngọc Hà'
 WHEN 'CTLUAT' THEN N'ThS. Trần Quốc Bảo'
 WHEN 'DDT' THEN N'ThS. Lê Minh Tuấn'
 WHEN 'DTTT' THEN N'ThS. Nguyễn Thị Bảo Trân'
 WHEN 'GTNL' THEN N'ThS. Võ Hoàng Phúc'
 WHEN 'INTT' THEN N'ThS. Đặng Thị Thu Hương'
 WHEN 'KHUD' THEN N'ThS. Bùi Quang Huy'
 WHEN 'KINHTE' THEN N'ThS. Nguyễn Thị Thanh Thảo'
 WHEN 'NGOAINGU' THEN N'ThS. Trần Thị Mai Anh'
 WHEN 'TTDL' THEN N'ThS. Phan Ngọc Diễm'
 WHEN 'VSPKT' THEN N'ThS. Lê Quốc Khánh'
 WHEN 'XAYDUNG' THEN N'ThS. Nguyễn Hoàng Nam'
 WHEN 'CNTT' THEN N'ThS. Phạm Minh Đức'
 ELSE N'ThS. Nguyễn Minh An' END, NgayCapNhat = DATEADD(HOUR,7,SYSUTCDATETIME())
FROM dbo.TaiKhoan t JOIN dbo.NguoiTuVan c ON c.MaTK=t.MaTK
JOIN dbo.DonVi d ON d.MaDonVi=c.MaDonVi
WHERE t.VaiTro='CoVan' AND t.HoTen LIKE N'Cố vấn mẫu%';
UPDATE dbo.NguoiTuVan SET ChucVu=N'Cố vấn học tập'
WHERE ChucVu LIKE N'Cố vấn mẫu%';
