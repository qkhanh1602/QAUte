-- Idempotent policy migration; existing timestamps are preserved.
-- Application and datetime2 values use Asia/Ho_Chi_Minh local wall time.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
UPDATE dbo.YeuCauTuVan SET HanTiepNhan=DATEADD(HOUR,48,NgayTao), HanSLA=DATEADD(HOUR,48,NgayTao)
WHERE TrangThai IN ('ChoTiepNhan','DangXuLy','DaPhanHoi')
 AND (HanTiepNhan<>DATEADD(HOUR,48,NgayTao) OR HanSLA<>DATEADD(HOUR,48,NgayTao));
UPDATE l SET HanDuyet=k.BatDau FROM dbo.LichHenTuVan l JOIN dbo.KhungGioTuVan k ON k.MaKhungGio=l.MaKhungGio
WHERE l.TrangThai='ChoDuyet' AND l.HanDuyet>k.BatDau;
UPDATE dbo.CauHinhHeThong SET GiaTri=N'ELAPSED', MoTa=N'SLA tính giờ liên tục kể từ khi gửi; giờ trực cố vấn cấu hình riêng' WHERE Khoa='sla.clock';
UPDATE dbo.CauHinhHeThong SET GiaTri=N'48', MoTa=N'Hạn tiếp nhận và phản hồi 48 giờ liên tục; cảnh báo khi quá hạn' WHERE Khoa='sla.accept_hours';
UPDATE dbo.CauHinhHeThong SET MoTa=N'Datetime2 lưu giờ Việt Nam Asia/Ho_Chi_Minh, không tự cộng thêm 7 giờ khi hiển thị' WHERE Khoa='app.timezone';
DECLARE @table nvarchar(258), @column nvarchar(128), @constraint nvarchar(128), @command nvarchar(max);
DECLARE defaults_cursor CURSOR LOCAL FAST_FORWARD FOR
 SELECT QUOTENAME(OBJECT_SCHEMA_NAME(d.parent_object_id))+N'.'+QUOTENAME(OBJECT_NAME(d.parent_object_id)),
 QUOTENAME(c.name),QUOTENAME(d.name)
 FROM sys.default_constraints d JOIN sys.columns c ON c.object_id=d.parent_object_id AND c.column_id=d.parent_column_id
 WHERE d.definition LIKE '%SYSUTCDATETIME%' AND d.definition NOT LIKE '%DATEADD%'
 AND OBJECT_SCHEMA_NAME(d.parent_object_id)='dbo';
OPEN defaults_cursor;
FETCH NEXT FROM defaults_cursor INTO @table,@column,@constraint;
WHILE @@FETCH_STATUS=0
BEGIN
 SET @command=N'ALTER TABLE '+@table+N' DROP CONSTRAINT '+@constraint+N'; ALTER TABLE '+@table
   +N' ADD CONSTRAINT '+@constraint+N' DEFAULT (DATEADD(HOUR,7,SYSUTCDATETIME())) FOR '+@column+N';';
 EXEC sp_executesql @command;
 FETCH NEXT FROM defaults_cursor INTO @table,@column,@constraint;
END;
CLOSE defaults_cursor;
DEALLOCATE defaults_cursor;
EXEC(N'CREATE OR ALTER VIEW dbo.vw_ThongKeYeuCauTheoNgay AS
 SELECT CAST(NgayTao AS date) AS NgayVietNam,MaDonVi,MaLinhVuc,COUNT_BIG(*) AS TongYeuCau,
 SUM(CAST(CASE WHEN TrangThai IN (''DaPhanHoi'',''Dong'') THEN 1 ELSE 0 END AS bigint)) AS DaHoanTat,
 CAST(100.0*SUM(CASE WHEN TrangThai IN (''DaPhanHoi'',''Dong'') THEN 1 ELSE 0 END)/NULLIF(COUNT_BIG(*),0) AS decimal(6,2)) AS TyLeHoanTat
 FROM dbo.YeuCauTuVan GROUP BY CAST(NgayTao AS date),MaDonVi,MaLinhVuc;');
COMMIT;
