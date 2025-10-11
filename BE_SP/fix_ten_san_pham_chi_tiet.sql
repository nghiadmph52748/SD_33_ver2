-- Script để sửa lỗi cột ten_san_pham_chi_tiet không tồn tại
-- Chạy script này để thêm cột và cập nhật dữ liệu

-- Kiểm tra xem cột đã tồn tại chưa
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS 
               WHERE TABLE_NAME = 'chi_tiet_san_pham' 
               AND COLUMN_NAME = 'ten_san_pham_chi_tiet')
BEGIN
    -- Thêm cột "ten_san_pham_chi_tiet" vào bảng chi_tiet_san_pham
    ALTER TABLE chi_tiet_san_pham 
    ADD COLUMN ten_san_pham_chi_tiet NVARCHAR(500);
    
    PRINT 'Đã thêm cột ten_san_pham_chi_tiet vào bảng chi_tiet_san_pham';
END
ELSE
BEGIN
    PRINT 'Cột ten_san_pham_chi_tiet đã tồn tại trong bảng chi_tiet_san_pham';
END

-- Cập nhật dữ liệu cho cột mới từ các bảng liên quan
UPDATE chi_tiet_san_pham 
SET ten_san_pham_chi_tiet = CONCAT(
    ISNULL(sp.ten_san_pham, 'Sản phẩm không xác định'),
    CASE WHEN ms.ten_mau_sac IS NOT NULL AND ms.ten_mau_sac != '' 
         THEN CONCAT(' - ', ms.ten_mau_sac) ELSE '' END,
    CASE WHEN kt.ten_kich_thuoc IS NOT NULL AND kt.ten_kich_thuoc != '' 
         THEN CONCAT(' - Size ', kt.ten_kich_thuoc) ELSE '' END
)
FROM chi_tiet_san_pham cts
LEFT JOIN san_pham sp ON cts.id_san_pham = sp.id
LEFT JOIN mau_sac ms ON cts.id_mau_sac = ms.id
LEFT JOIN kich_thuoc kt ON cts.id_kich_thuoc = kt.id
WHERE cts.ten_san_pham_chi_tiet IS NULL OR cts.ten_san_pham_chi_tiet = '';

PRINT 'Đã cập nhật dữ liệu cho cột ten_san_pham_chi_tiet';

-- Thêm comment cho cột (chỉ cho SQL Server)
IF EXISTS (SELECT * FROM sys.databases WHERE name = DB_NAME())
BEGIN
    EXEC sp_addextendedproperty 
        @name = N'MS_Description', 
        @value = N'Tên sản phẩm chi tiết bao gồm tên sản phẩm, màu sắc và kích thước', 
        @level0type = N'SCHEMA', @level0name = N'dbo', 
        @level1type = N'TABLE', @level1name = N'chi_tiet_san_pham', 
        @level2type = N'COLUMN', @level2name = N'ten_san_pham_chi_tiet';
    
    PRINT 'Đã thêm comment cho cột ten_san_pham_chi_tiet';
END

-- Kiểm tra kết quả
SELECT TOP 10 
    id,
    ma_chi_tiet_san_pham,
    ten_san_pham_chi_tiet,
    so_luong,
    gia_ban
FROM chi_tiet_san_pham 
WHERE ten_san_pham_chi_tiet IS NOT NULL
ORDER BY id;

PRINT 'Script hoàn thành! Cột ten_san_pham_chi_tiet đã được thêm và cập nhật dữ liệu.';
