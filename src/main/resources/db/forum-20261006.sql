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
