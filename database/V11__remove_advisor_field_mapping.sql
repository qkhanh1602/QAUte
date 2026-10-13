-- Manual migration AFTER deploying code without the advisor-field JPA mapping.
-- Back up dbo.CoVanLinhVuc (DDL and all rows) outside Git before applying.
-- Dry run is the default. To apply, set @Apply=1 and @BackupRowCount to the
-- independently verified number of rows in the external backup.
-- New code works with or without this table. Restore it BEFORE rolling back
-- to an application version that still maps the many-to-many relationship.
SET NOCOUNT ON;
SET XACT_ABORT ON;
DECLARE @Apply bit = 0;
DECLARE @BackupRowCount bigint = NULL;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @lockResult int;
 EXEC @lockResult=sys.sp_getapplock @Resource=N'QAUTE-V11-advisor-fields',
  @LockMode='Exclusive',@LockOwner='Transaction',@LockTimeout=10000;
 IF @lockResult<0 THROW 51110,'V11 could not acquire migration lock.',1;
 DECLARE @tableId int=OBJECT_ID(N'dbo.CoVanLinhVuc',N'U');
 IF @tableId IS NULL
 BEGIN
  COMMIT;
  SELECT N'Already absent; no changes made' AS Result;
  RETURN;
 END;
 IF (SELECT COUNT(*) FROM sys.columns WHERE object_id=@tableId)<>2
  OR NOT EXISTS(SELECT 1 FROM sys.columns WHERE object_id=@tableId AND name=N'MaCV' AND system_type_id=56 AND is_nullable=0)
  OR NOT EXISTS(SELECT 1 FROM sys.columns WHERE object_id=@tableId AND name=N'MaLinhVuc' AND system_type_id=56 AND is_nullable=0)
  THROW 51111,'Unexpected table shape; inspect and back up custom data before removal.',1;
 IF EXISTS(SELECT 1 FROM sys.foreign_keys WHERE referenced_object_id=@tableId AND parent_object_id<>@tableId)
  OR EXISTS(SELECT 1 FROM sys.sql_expression_dependencies WHERE referenced_id=@tableId AND referencing_id<>@tableId)
  OR EXISTS(SELECT 1 FROM sys.triggers WHERE parent_id=@tableId)
  THROW 51112,'Dependent objects exist; inspect them before removing the mapping.',1;
 DECLARE @rowCount bigint;
 SELECT @rowCount=COUNT_BIG(*) FROM dbo.CoVanLinhVuc WITH(TABLOCKX,HOLDLOCK);
 IF EXISTS(SELECT 1 FROM dbo.CoVanLinhVuc m
  LEFT JOIN dbo.NguoiTuVan a ON a.MaCV=m.MaCV
  LEFT JOIN dbo.LinhVucTuVan f ON f.MaLinhVuc=m.MaLinhVuc
  WHERE a.MaCV IS NULL OR f.MaLinhVuc IS NULL OR a.MaDonVi<>f.MaDonVi)
  THROW 51113,'Orphaned or cross-unit mappings exist; inspect them before removal.',1;
 IF @Apply=0
 BEGIN
  ROLLBACK;
  SELECT N'Dry run passed; table preserved' AS Result,@rowCount AS RowsToBackUp;
  RETURN;
 END;
 IF @BackupRowCount IS NULL OR @BackupRowCount<>@rowCount
  THROW 51114,'Provide the verified external backup row count; no table was removed.',1;
 DROP TABLE dbo.CoVanLinhVuc;
 COMMIT;
 SELECT N'Advisor-field mapping removed; business records preserved' AS Result,@rowCount AS BackedUpRows;
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK;
 THROW;
END CATCH;
