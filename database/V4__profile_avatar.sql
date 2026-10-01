/* Chạy một lần trên database QAUTE hiện có trước khi dùng chức năng ảnh đại diện. */
IF COL_LENGTH('dbo.TaiKhoan', 'AvatarUrl') IS NULL
    ALTER TABLE dbo.TaiKhoan ADD AvatarUrl varchar(1000) NULL;
GO

IF COL_LENGTH('dbo.TaiKhoan', 'AvatarPublicId') IS NULL
    ALTER TABLE dbo.TaiKhoan ADD AvatarPublicId varchar(500) NULL;
GO
