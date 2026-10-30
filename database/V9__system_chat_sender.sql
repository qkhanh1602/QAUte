-- Apply before deploying system chat replies. Existing messages are preserved.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
IF EXISTS (SELECT 1 FROM sys.columns WHERE object_id=OBJECT_ID('dbo.TinNhanChat')
 AND name='MaTK_Gui' AND is_nullable=0)
 ALTER TABLE dbo.TinNhanChat ALTER COLUMN MaTK_Gui int NULL;
COMMIT;
