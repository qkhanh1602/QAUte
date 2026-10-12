-- Run manually only AFTER deploying code without the three unused JPA entities.
-- Back up schema/data first. Intentionally NOT run by WorkflowPolicyInitializer.
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF EXISTS (
  SELECT 1 FROM sys.foreign_keys
  WHERE referenced_object_id IN (OBJECT_ID('dbo.DanhTinhSSO'),OBJECT_ID('dbo.PhienDangNhap'),
   OBJECT_ID('dbo.TokenDatLaiMatKhau'),OBJECT_ID('dbo.LichLamViec'),OBJECT_ID('dbo.NgayNghi'),OBJECT_ID('dbo.NhatKyHeThong'))
 ) THROW 51000, 'A foreign key references a retired table. Review before migration.', 1;
 -- Do not silently discard newly created business records.
 IF OBJECT_ID('dbo.DanhTinhSSO','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.DanhTinhSSO WITH (TABLOCKX,HOLDLOCK)) THROW 51001,''DanhTinhSSO is not empty.'',1;');
 IF OBJECT_ID('dbo.PhienDangNhap','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.PhienDangNhap WITH (TABLOCKX,HOLDLOCK)) THROW 51001,''PhienDangNhap is not empty.'',1;');
 IF OBJECT_ID('dbo.TokenDatLaiMatKhau','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.TokenDatLaiMatKhau WITH (TABLOCKX,HOLDLOCK)) THROW 51001,''TokenDatLaiMatKhau is not empty.'',1;');
 IF OBJECT_ID('dbo.NgayNghi','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.NgayNghi WITH (TABLOCKX,HOLDLOCK)) THROW 51001,''NgayNghi is not empty.'',1;');
 IF OBJECT_ID('dbo.NhatKyHeThong','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.NhatKyHeThong WITH (TABLOCKX,HOLDLOCK)) THROW 51001,''NhatKyHeThong is not empty.'',1;');
 IF OBJECT_ID('dbo.LichLamViec','U') IS NOT NULL
  EXEC(N'IF EXISTS(SELECT 1 FROM dbo.LichLamViec WITH (TABLOCKX,HOLDLOCK)
   WHERE ThuISO NOT BETWEEN 1 AND 5 OR NOT (
    (GioBatDau=CAST(''08:00'' AS time) AND GioKetThuc=CAST(''12:00'' AS time)) OR
    (GioBatDau=CAST(''13:00'' AS time) AND GioKetThuc=CAST(''17:00'' AS time))))
   THROW 51002,''LichLamViec has custom hours. Back up and review before migration.'',1;');
 DROP TABLE IF EXISTS dbo.DanhTinhSSO;
 DROP TABLE IF EXISTS dbo.PhienDangNhap;
 DROP TABLE IF EXISTS dbo.TokenDatLaiMatKhau;
 DROP TABLE IF EXISTS dbo.LichLamViec;
 DROP TABLE IF EXISTS dbo.NgayNghi;
 DROP TABLE IF EXISTS dbo.NhatKyHeThong;
 COMMIT;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
