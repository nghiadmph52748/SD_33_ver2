-- Thêm cột "ten_san_pham_chi_tiet" vào bảng chi_tiet_san_pham
ALTER TABLE chi_tiet_san_pham 
ADD COLUMN ten_san_pham_chi_tiet NVARCHAR(500);

-- Cập nhật dữ liệu mẫu cho cột mới
UPDATE chi_tiet_san_pham 
SET ten_san_pham_chi_tiet = CONCAT(
    sp.ten_san_pham,
    CASE WHEN ms.ten_mau_sac IS NOT NULL THEN CONCAT(' - ', ms.ten_mau_sac) ELSE '' END,
    CASE WHEN kt.ten_kich_thuoc IS NOT NULL THEN CONCAT(' - ', kt.ten_kich_thuoc) ELSE '' END
)
FROM chi_tiet_san_pham cts
LEFT JOIN san_pham sp ON cts.id_san_pham = sp.id
LEFT JOIN mau_sac ms ON cts.id_mau_sac = ms.id
LEFT JOIN kich_thuoc kt ON cts.id_kich_thuoc = kt.id
WHERE cts.ten_san_pham_chi_tiet IS NULL;

-- Thêm comment cho cột
EXEC sp_addextendedproperty 
    @name = N'MS_Description', 
    @value = N'Tên sản phẩm chi tiết bao gồm tên sản phẩm, màu sắc và kích thước', 
    @level0type = N'SCHEMA', @level0name = N'dbo', 
    @level1type = N'TABLE', @level1name = N'chi_tiet_san_pham', 
    @level2type = N'COLUMN', @level2name = N'ten_san_pham_chi_tiet';

