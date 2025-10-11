package org.example.be_sp.model.response;

import java.math.BigDecimal;

import org.example.be_sp.entity.HoaDonChiTiet;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanPhamDaBanResponse {
    private Integer id;
    private String maHoaDonChiTiet;
    private String tenSanPhamChiTiet;
    private String maSanPhamChiTiet;
    private String tenSanPham;
    private String maSanPham;
    private String tenMauSac;
    private String maMauSac;
    private String tenKichThuoc;
    private String maKichThuoc;
    private String tenDeGiay;
    private String maDeGiay;
    private String tenChatLieu;
    private String maChatLieu;
    private String tenTrongLuong;
    private String maTrongLuong;
    private Integer soLuong;
    private BigDecimal giaBan;
    private BigDecimal thanhTien;
    private String ghiChu;
    private String anhSanPham;

    public SanPhamDaBanResponse(HoaDonChiTiet hoaDonChiTiet) {
        if (hoaDonChiTiet == null) {
            return;
        }
        
        this.id = hoaDonChiTiet.getId();
        this.maHoaDonChiTiet = hoaDonChiTiet.getMaHoaDonChiTiet();
        this.soLuong = hoaDonChiTiet.getSoLuong();
        this.giaBan = hoaDonChiTiet.getGiaBan();
        this.thanhTien = hoaDonChiTiet.getThanhTien();
        this.ghiChu = hoaDonChiTiet.getGhiChu();
        
        // Lấy thông tin từ trường trực tiếp nếu có
        this.tenSanPhamChiTiet = hoaDonChiTiet.getTenSanPhamChiTiet();
        this.maSanPhamChiTiet = hoaDonChiTiet.getMaSanPhamChiTiet();
        
        // Lấy thông tin từ chi_tiet_san_pham
        if (hoaDonChiTiet.getIdChiTietSanPham() != null) {
            var chiTietSanPham = hoaDonChiTiet.getIdChiTietSanPham();
            
            // Mã chi tiết sản phẩm
            if (this.maSanPhamChiTiet == null || this.maSanPhamChiTiet.trim().isEmpty()) {
                this.maSanPhamChiTiet = chiTietSanPham.getMaChiTietSanPham();
            }
            
            // Thông tin sản phẩm chính
            if (chiTietSanPham.getIdSanPham() != null) {
                this.tenSanPham = chiTietSanPham.getIdSanPham().getTenSanPham();
                this.maSanPham = chiTietSanPham.getIdSanPham().getMaSanPham();
            }
            
            // Màu sắc
            if (chiTietSanPham.getIdMauSac() != null) {
                this.tenMauSac = chiTietSanPham.getIdMauSac().getTenMauSac();
                this.maMauSac = chiTietSanPham.getIdMauSac().getMaMauSac();
            }
            
            // Kích thước
            if (chiTietSanPham.getIdKichThuoc() != null) {
                this.tenKichThuoc = chiTietSanPham.getIdKichThuoc().getTenKichThuoc();
                this.maKichThuoc = chiTietSanPham.getIdKichThuoc().getMaKichThuoc();
            }
            
            // Đế giày
            if (chiTietSanPham.getIdDeGiay() != null) {
                this.tenDeGiay = chiTietSanPham.getIdDeGiay().getTenDeGiay();
                this.maDeGiay = chiTietSanPham.getIdDeGiay().getMaDeGiay();
            }
            
            // Chất liệu
            if (chiTietSanPham.getIdChatLieu() != null) {
                this.tenChatLieu = chiTietSanPham.getIdChatLieu().getTenChatLieu();
                this.maChatLieu = chiTietSanPham.getIdChatLieu().getMaChatLieu();
            }
            
            // Trọng lượng
            if (chiTietSanPham.getIdTrongLuong() != null) {
                this.tenTrongLuong = chiTietSanPham.getIdTrongLuong().getTenTrongLuong();
                this.maTrongLuong = chiTietSanPham.getIdTrongLuong().getMaTrongLuong();
            }
            
            // Tạo tên sản phẩm chi tiết nếu chưa có
            if (this.tenSanPhamChiTiet == null || this.tenSanPhamChiTiet.trim().isEmpty()) {
                this.tenSanPhamChiTiet = buildTenSanPhamChiTiet(chiTietSanPham);
            }
            
            // Lấy ảnh sản phẩm đầu tiên từ chi_tiet_san_pham_anh
            if (chiTietSanPham.getChiTietSanPhamAnhs() != null && !chiTietSanPham.getChiTietSanPhamAnhs().isEmpty()) {
                var firstImage = chiTietSanPham.getChiTietSanPhamAnhs().iterator().next();
                if (firstImage.getIdAnhSanPham() != null && firstImage.getIdAnhSanPham().getDuongDanAnh() != null) {
                    this.anhSanPham = firstImage.getIdAnhSanPham().getDuongDanAnh();
                }
            }
        }
    }
    
    /**
     * Tạo tên sản phẩm chi tiết từ các thuộc tính
     */
    private String buildTenSanPhamChiTiet(org.example.be_sp.entity.ChiTietSanPham chiTietSanPham) {
        StringBuilder tenSanPhamChiTiet = new StringBuilder();
        
        // Tên sản phẩm chính
        if (chiTietSanPham.getIdSanPham() != null && chiTietSanPham.getIdSanPham().getTenSanPham() != null) {
            tenSanPhamChiTiet.append(chiTietSanPham.getIdSanPham().getTenSanPham());
        }
        
        // Màu sắc
        if (chiTietSanPham.getIdMauSac() != null && chiTietSanPham.getIdMauSac().getTenMauSac() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdMauSac().getTenMauSac());
        }
        
        // Kích thước
        if (chiTietSanPham.getIdKichThuoc() != null && chiTietSanPham.getIdKichThuoc().getTenKichThuoc() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdKichThuoc().getTenKichThuoc());
        }
        
        // Đế giày
        if (chiTietSanPham.getIdDeGiay() != null && chiTietSanPham.getIdDeGiay().getTenDeGiay() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdDeGiay().getTenDeGiay());
        }
        
        // Chất liệu
        if (chiTietSanPham.getIdChatLieu() != null && chiTietSanPham.getIdChatLieu().getTenChatLieu() != null) {
            tenSanPhamChiTiet.append(" - ").append(chiTietSanPham.getIdChatLieu().getTenChatLieu());
        }
        
        return tenSanPhamChiTiet.toString();
    }
}
