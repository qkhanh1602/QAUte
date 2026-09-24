-- Apply once to the already-created QAUTE database.
-- Run from SQLCMD or SSMS with Query > SQLCMD Mode enabled.
:ON ERROR EXIT
USE QAUTE;
GO
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

IF OBJECT_ID(N'dbo.YeuCauXacMinhOTP', N'U') IS NULL
BEGIN
 CREATE TABLE dbo.YeuCauXacMinhOTP (
  MaOTP uniqueidentifier NOT NULL DEFAULT NEWID() PRIMARY KEY,
  MaTK int NULL REFERENCES dbo.TaiKhoan(MaTK),
  Email varchar(254) NOT NULL,
  MucDich varchar(30) NOT NULL CHECK (MucDich IN ('DangKy','XacMinhEmail','DatLaiMatKhau','DoiEmail')),
  OTPHash varchar(255) NOT NULL,
  NgayTao datetime2(3) NOT NULL DEFAULT SYSUTCDATETIME(),
  HetHan datetime2(3) NOT NULL, DaDungLuc datetime2(3) NULL,
  SoLanNhapSai tinyint NOT NULL DEFAULT 0 CHECK (SoLanNhapSai BETWEEN 0 AND 5),
  SoLanGui tinyint NOT NULL DEFAULT 1 CHECK (SoLanGui BETWEEN 1 AND 3),
  GuiLaiSau datetime2(3) NOT NULL, IPHash binary(32) NULL,
  CHECK (HetHan > NgayTao), CHECK (GuiLaiSau >= NgayTao),
  CHECK (DaDungLuc IS NULL OR DaDungLuc BETWEEN NgayTao AND HetHan)
 );
 CREATE INDEX IX_OTP_TraCuu ON dbo.YeuCauXacMinhOTP(Email, MucDich, NgayTao DESC)
  INCLUDE(HetHan, DaDungLuc, SoLanNhapSai, GuiLaiSau);
END;

IF OBJECT_ID(N'dbo.NhatKyBaoMat', N'U') IS NULL
BEGIN
 CREATE TABLE dbo.NhatKyBaoMat (
  MaSuKien bigint IDENTITY PRIMARY KEY, MaTK int NULL REFERENCES dbo.TaiKhoan(MaTK),
  LoaiSuKien varchar(50) NOT NULL, ThanhCong bit NOT NULL,
  IPHash binary(32) NULL, UserAgentHash binary(32) NULL, ChiTiet nvarchar(500) NULL,
  NgayTao datetime2(3) NOT NULL DEFAULT SYSUTCDATETIME()
 );
 CREATE INDEX IX_NhatKyBaoMat_TaiKhoan ON dbo.NhatKyBaoMat(MaTK, NgayTao DESC);
 CREATE INDEX IX_NhatKyBaoMat_IP ON dbo.NhatKyBaoMat(IPHash, NgayTao DESC) WHERE IPHash IS NOT NULL;
END;

INSERT dbo.CauHinhHeThong(Khoa,GiaTri,MoTa)
SELECT v.Khoa,v.GiaTri,v.MoTa FROM (VALUES
 ('security.otp.expiry_minutes',N'10',N'OTP có hiệu lực 10 phút'),
 ('security.otp.max_verify_attempts',N'5',N'Số lần nhập OTP sai tối đa'),
 ('security.otp.resend_cooldown_seconds',N'60',N'Chờ tối thiểu trước khi gửi lại OTP'),
 ('security.otp.max_sends_per_hour',N'3',N'Số OTP gửi tối đa cho mỗi email/IP mỗi giờ'),
 ('security.login.max_failures',N'5',N'Đăng nhập sai tối đa trước khi khóa tạm'),
 ('security.login.lock_minutes',N'15',N'Thời gian khóa tạm sau nhiều lần đăng nhập sai')
) v(Khoa,GiaTri,MoTa)
WHERE NOT EXISTS (SELECT 1 FROM dbo.CauHinhHeThong c WHERE c.Khoa=v.Khoa);
COMMIT;
GO
PRINT N'Security hardening V2 applied successfully.';
