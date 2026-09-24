-- ============================================================================
-- Migration V3: Add all official faculties of HCMUTE to dbo.DonVi
-- ============================================================================
USE QAUTE;
GO

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'CKCTM')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('CKCTM', N'Khoa Cơ khí Chế tạo máy', 'Khoa', 1, N'Khoa Cơ khí Chế tạo máy - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'GTNL')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('GTNL', N'Khoa Giao thông và năng lượng', 'Khoa', 1, N'Khoa Giao thông và Năng lượng - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'CNHHTP')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('CNHHTP', N'Khoa Công nghệ Hóa học và Thực phẩm', 'Khoa', 1, N'Khoa Công nghệ Hóa học và Thực phẩm - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'DDT')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('DDT', N'Khoa Điện - Điện tử', 'Khoa', 1, N'Khoa Điện - Điện tử - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'INTT')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('INTT', N'Khoa In và Truyền thông', 'Khoa', 1, N'Khoa In và Truyền thông - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'KHUD')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('KHUD', N'Khoa Khoa học Ứng dụng', 'Khoa', 1, N'Khoa Khoa học Ứng dụng - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'KINHTE')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('KINHTE', N'Khoa Kinh tế', 'Khoa', 1, N'Khoa Kinh tế - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'NGOAINGU')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('NGOAINGU', N'Khoa Ngoại ngữ', 'Khoa', 1, N'Khoa Ngoại ngữ - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'TTDL')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('TTDL', N'Khoa Thời trang và Du lịch', 'Khoa', 1, N'Khoa Thời trang và Du lịch - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'XAYDUNG')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('XAYDUNG', N'Khoa Xây dựng', 'Khoa', 1, N'Khoa Xây dựng - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'CTLUAT')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('CTLUAT', N'Khoa Chính trị và Luật', 'Khoa', 1, N'Khoa Chính trị và Luật - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'VSPKT')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('VSPKT', N'Viện Sư phạm Kỹ thuật', 'Khoa', 1, N'Viện Sư phạm Kỹ thuật - HCMUTE');

IF NOT EXISTS (SELECT 1 FROM dbo.DonVi WHERE MaDonViCode = 'DTTT')
    INSERT INTO dbo.DonVi (MaDonViCode, TenDonVi, LoaiDonVi, HoatDong, MoTa) VALUES ('DTTT', N'Khoa Đào tạo Tiên tiến', 'Khoa', 1, N'Khoa Đào tạo Tiên tiến (Chương trình dạy bằng Tiếng Anh) - HCMUTE');
GO
