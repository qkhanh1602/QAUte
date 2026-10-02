-- Run against the QAUTE database, including older installs created from QAUTE.sql.
IF COL_LENGTH('dbo.YeuCauXacMinhOTP', 'SoDienThoai') IS NULL
    ALTER TABLE dbo.YeuCauXacMinhOTP ADD SoDienThoai varchar(15) NULL;
GO
IF COL_LENGTH('dbo.YeuCauXacMinhOTP', 'KenhXacMinh') IS NULL
    ALTER TABLE dbo.YeuCauXacMinhOTP ADD KenhXacMinh varchar(10) NULL;
GO
