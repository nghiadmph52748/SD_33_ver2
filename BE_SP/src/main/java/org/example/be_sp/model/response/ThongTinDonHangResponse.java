package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import org.example.be_sp.entity.ThongTinDonHang;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ThongTinDonHangResponse {
    private Integer id;
    private String maDonHang;
    private String tenTrangThaiDonHang;
    private String maThongTinDonHang;
    private LocalDate thoiGian;
    private String ghiChu;
    private Boolean deleted;
    
    // Thông tin từ hoa_don
    private LocalDate ngayTao;
    private LocalDate ngayThanhToan;
    private String tenNhanVien;
    private String maNhanVien;
    
    // Tổng tiền hàng tính từ hoa_don_chi_tiet
    private BigDecimal tongTienHang;

    public ThongTinDonHangResponse(ThongTinDonHang data) {
        this.id = data.getId();
        this.maDonHang = data.getIdHoaDon().getMaHoaDon();
        this.tenTrangThaiDonHang = data.getIdTrangThaiDonHang().getTenTrangThaiDonHang();
        this.maThongTinDonHang = data.getMaThongTinDonHang();
        this.thoiGian = data.getThoiGian();
        this.ghiChu = data.getGhiChu();
        this.deleted = data.getDeleted();
        
        // Lấy thông tin từ hoa_don
        if (data.getIdHoaDon() != null) {
            this.ngayTao = data.getIdHoaDon().getNgayTao();
            this.ngayThanhToan = data.getIdHoaDon().getNgayThanhToan();
            
            // Ưu tiên lấy từ trường trực tiếp, fallback về quan hệ
            if (data.getIdHoaDon().getTenNhanVien() != null && !data.getIdHoaDon().getTenNhanVien().trim().isEmpty()) {
                this.tenNhanVien = data.getIdHoaDon().getTenNhanVien();
            } else if (data.getIdHoaDon().getIdNhanVien() != null) {
                this.tenNhanVien = data.getIdHoaDon().getIdNhanVien().getTenNhanVien();
            }
            
            if (data.getIdHoaDon().getMaNhanVien() != null && !data.getIdHoaDon().getMaNhanVien().trim().isEmpty()) {
                this.maNhanVien = data.getIdHoaDon().getMaNhanVien();
            } else if (data.getIdHoaDon().getIdNhanVien() != null) {
                this.maNhanVien = data.getIdHoaDon().getIdNhanVien().getMaNhanVien();
            }
            
            // Tính tổng tiền hàng từ hoa_don_chi_tiet
            this.tongTienHang = calculateTongTienHang(data.getIdHoaDon());
        }
    }
    
    /**
     * Tính tổng tiền hàng từ các chi tiết hóa đơn
     */
    private BigDecimal calculateTongTienHang(org.example.be_sp.entity.HoaDon hoaDon) {
        if (hoaDon.getHoaDonChiTiets() == null || hoaDon.getHoaDonChiTiets().isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return hoaDon.getHoaDonChiTiets().stream()
            .filter(item -> item.getThanhTien() != null && !item.getDeleted())
            .map(item -> item.getThanhTien())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
