USE QAUTE;
GO

-- Cho phép tài khoản cố vấn tồn tại ở trạng thái chờ Admin xác minh.
DECLARE @ConstraintName sysname;
DECLARE @DropSql nvarchar(max);
SELECT TOP (1) @ConstraintName = cc.name
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID(N'dbo.TaiKhoan')
  AND cc.definition LIKE '%TrangThai%';

IF @ConstraintName IS NOT NULL
BEGIN
    SET @DropSql = N'ALTER TABLE dbo.TaiKhoan DROP CONSTRAINT ' + QUOTENAME(@ConstraintName) + N';';
    EXEC sys.sp_executesql @DropSql;
END;
GO

ALTER TABLE dbo.TaiKhoan WITH CHECK
ADD CONSTRAINT CK_TaiKhoan_TrangThai
CHECK (TrangThai IN ('ChoDuyet', 'HoatDong', 'Khoa'));
GO

ALTER TABLE dbo.TaiKhoan CHECK CONSTRAINT CK_TaiKhoan_TrangThai;
GO

-- Cho phép dùng OTP riêng cho luồng đăng ký cố vấn.
DECLARE @OtpConstraintName sysname;
DECLARE @DropOtpSql nvarchar(max);
SELECT TOP (1) @OtpConstraintName = cc.name
FROM sys.check_constraints cc
WHERE cc.parent_object_id = OBJECT_ID(N'dbo.YeuCauXacMinhOTP')
  AND cc.definition LIKE '%MucDich%';

IF @OtpConstraintName IS NOT NULL
BEGIN
    SET @DropOtpSql = N'ALTER TABLE dbo.YeuCauXacMinhOTP DROP CONSTRAINT ' + QUOTENAME(@OtpConstraintName) + N';';
    EXEC sys.sp_executesql @DropOtpSql;
END;
GO

ALTER TABLE dbo.YeuCauXacMinhOTP WITH CHECK
ADD CONSTRAINT CK_YeuCauXacMinhOTP_MucDich
CHECK (MucDich IN ('DangKy', 'DangKyCoVan', 'XacMinhEmail', 'DatLaiMatKhau', 'DoiEmail'));
GO

ALTER TABLE dbo.YeuCauXacMinhOTP CHECK CONSTRAINT CK_YeuCauXacMinhOTP_MucDich;
GO
