-- QD11 acceptance deadline is separate from QD25 yellow warning.
SET XACT_ABORT ON;
BEGIN TRAN;
UPDATE dbo.CauHinhHeThong SET GiaTri=N'48', MoTa=N'Hạn tiếp nhận và phản hồi 48 giờ liên tục; cảnh báo khi quá hạn', NgayCapNhat=SYSUTCDATETIME()
WHERE Khoa=N'sla.accept_hours' AND GiaTri=N'24';
COMMIT;
