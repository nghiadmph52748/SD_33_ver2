-- Script đơn giản để sửa lỗi cột ten_san_pham_chi_tiet
-- Chạy script này trong SQL Server Management Studio hoặc Azure Data Studio

-- Thêm cột nếu chưa tồn tại
ALTER TABLE chi_tiet_san_pham 
ADD ten_san_pham_chi_tiet NVARCHAR(500);

-- Cập nhật dữ liệu
UPDATE chi_tiet_san_pham 
SET ten_san_pham_chi_tiet = CONCAT(
    sp.ten_san_pham,
    ' - ',
    ms.ten_mau_sac,
    ' - Size ',
    kt.ten_kich_thuoc
)
FROM chi_tiet_san_pham cts
JOIN san_pham sp ON cts.id_san_pham = sp.id
JOIN mau_sac ms ON cts.id_mau_sac = ms.id
JOIN kich_thuoc kt ON cts.id_kich_thuoc = kt.id;
