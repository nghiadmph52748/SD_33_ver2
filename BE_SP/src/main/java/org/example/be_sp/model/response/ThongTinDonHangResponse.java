package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ThongTinDonHang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThongTinDonHangResponse {
    private Integer id;
    private String maDonHang;
    private String tenTrangThaiDonHang;
    private String maThongTinDonHang;
    private LocalDateTime thoiGian;
    private String ghiChu;
    private Boolean deleted;
    
    // Thông tin từ hoa_don
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private String tenNhanVien;
    private String maNhanVien;
    
    // Thông tin khách hàng từ hoa_don
    private String tenKhachHang;
    private String maKhachHang;
    private String emailKhachHang;
    private String soDienThoaiKhachHang;
    private String diaChiKhachHang;
    
    // Tổng tiền hàng tính từ hoa_don_chi_tiet
    private BigDecimal tongTienHang;
    
    // Danh sách sản phẩm đã bán từ hoa_don_chi_tiet
    private List<SanPhamDaBanResponse> danhSachSanPhamDaBan;

    public ThongTinDonHangResponse(ThongTinDonHang data) {
        if (data == null) {
            return;
        }
        
        this.id = data.getId();
        this.maThongTinDonHang = data.getMaThongTinDonHang();
        this.thoiGian = data.getThoiGian();
        this.ghiChu = data.getGhiChu();
        this.deleted = data.getDeleted();
        
        // Lấy thông tin từ hoa_don
        if (data.getIdHoaDon() != null) {
            this.maDonHang = data.getIdHoaDon().getMaHoaDon();
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
            
            // Lấy thông tin khách hàng từ hoa_don
            if (data.getIdHoaDon().getIdKhachHang() != null) {
                this.tenKhachHang = data.getIdHoaDon().getIdKhachHang().getTenKhachHang();
                this.maKhachHang = data.getIdHoaDon().getIdKhachHang().getMaKhachHang();
                this.emailKhachHang = data.getIdHoaDon().getIdKhachHang().getEmail();
                this.soDienThoaiKhachHang = data.getIdHoaDon().getIdKhachHang().getSoDienThoai();
                this.diaChiKhachHang = data.getIdHoaDon().getIdKhachHang().getDiaChi();
            }
            
            // Tính tổng tiền hàng từ hoa_don_chi_tiet
            this.tongTienHang = calculateTongTienHang(data.getIdHoaDon());
            
            // Lấy danh sách sản phẩm đã bán từ hoa_don_chi_tiet
            this.danhSachSanPhamDaBan = data.getIdHoaDon().getHoaDonChiTiets()
                .stream()
                .filter(item -> item != null && !item.getDeleted())
                .map(SanPhamDaBanResponse::new)
                .collect(Collectors.toList());
        }
        
        // Lấy thông tin trạng thái đơn hàng
        if (data.getIdTrangThaiDonHang() != null) {
            this.tenTrangThaiDonHang = data.getIdTrangThaiDonHang().getTenTrangThaiDonHang();
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
