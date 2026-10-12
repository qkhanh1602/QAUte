-- Remove only slots created by the retired demo appointment seeder.
-- Keep every slot referenced by an appointment, including historical appointments.
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET ARITHABORT ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DELETE k FROM dbo.KhungGioTuVan k
WHERE k.DiaDiem_Link LIKE N'Phòng tư vấn (lịch kiểm thử) - %'
AND NOT EXISTS (SELECT 1 FROM dbo.LichHenTuVan l WHERE l.MaKhungGio=k.MaKhungGio);

UPDATE dbo.KhungGioTuVan
SET DiaDiem_Link=REPLACE(DiaDiem_Link,N' (lịch kiểm thử)',N'')
WHERE DiaDiem_Link LIKE N'Phòng tư vấn (lịch kiểm thử) - %';

UPDATE dbo.LichHenTuVan
SET LinkHop_Phong=REPLACE(LinkHop_Phong,N' (lịch kiểm thử)',N'')
WHERE LinkHop_Phong LIKE N'Phòng tư vấn (lịch kiểm thử) - %';

COMMIT TRANSACTION;
