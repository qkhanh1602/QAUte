-- Keep the documented setting aligned with the 3-hour rule in AppointmentService.
SET XACT_ABORT ON;
BEGIN TRANSACTION;
UPDATE dbo.CauHinhHeThong SET GiaTri=N'3',MoTa=N'Hủy trước ít nhất 3 giờ cho cả hai hình thức',
 NgayCapNhat=DATEADD(HOUR,7,SYSUTCDATETIME()) WHERE Khoa='booking.cancel_before_hours';
COMMIT;
