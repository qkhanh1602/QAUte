-- Student proposals own immutable time records, including canceled/rejected history.
-- Retain all rows and foreign keys. Active overlap is serialized by the advisor account lock.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
DECLARE @constraint sysname, @statement nvarchar(max);
DECLARE proposal_constraints CURSOR LOCAL FAST_FORWARD FOR
SELECT kc.name FROM sys.key_constraints kc
WHERE kc.parent_object_id=OBJECT_ID(N'dbo.KhungGioTuVan') AND kc.type='UQ'
AND (SELECT COUNT(*) FROM sys.index_columns ic WHERE ic.object_id=kc.parent_object_id AND ic.index_id=kc.unique_index_id AND ic.key_ordinal>0)=2
AND EXISTS (SELECT 1 FROM sys.index_columns ic JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id WHERE ic.object_id=kc.parent_object_id AND ic.index_id=kc.unique_index_id AND c.name=N'MaCV')
AND EXISTS (SELECT 1 FROM sys.index_columns ic JOIN sys.columns c ON c.object_id=ic.object_id AND c.column_id=ic.column_id WHERE ic.object_id=kc.parent_object_id AND ic.index_id=kc.unique_index_id AND c.name=N'BatDau');
OPEN proposal_constraints;
FETCH NEXT FROM proposal_constraints INTO @constraint;
WHILE @@FETCH_STATUS=0
BEGIN
    SET @statement=N'ALTER TABLE dbo.KhungGioTuVan DROP CONSTRAINT '+QUOTENAME(@constraint);
    EXEC sp_executesql @statement;
    FETCH NEXT FROM proposal_constraints INTO @constraint;
END;
CLOSE proposal_constraints;
DEALLOCATE proposal_constraints;
IF OBJECT_ID(N'dbo.CauHinhHeThong',N'U') IS NOT NULL
    DELETE FROM dbo.CauHinhHeThong WHERE Khoa=N'booking.min_weekly_slots';
COMMIT TRANSACTION;
