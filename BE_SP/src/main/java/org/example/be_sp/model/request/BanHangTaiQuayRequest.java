package org.example.be_sp.model.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BanHangTaiQuayRequest {
    private Integer idKhachHang;
    private Integer idPhieuGiamGia;
    private Integer idNhanVien;
    private Integer idPhuongThucThanhToan;
    private Integer idTrangThaiDonHang;
    private HashMap<Integer, Integer> danhSachSanPham; // key: idSanPham, value: soLuong
    private List<HoaDonChiTietRequest> hoaDonChiTiet; // For orders from banHangMain
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTien;
    private BigDecimal tongTienSauGiam;
    private String tenNguoiNhan;
    private String diaChiNhanHang;
    private String soDienThoaiNguoiNhan;
    private String emailNguoiNhan;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private Boolean loaiDon;
    private Boolean trangThai;
    private Boolean deleted;
    private String ghiChu;
    private LocalDateTime createAt;
    private Integer createBy;
    private LocalDateTime updateAt;
    private Integer updateBy;
    
    // Các trường mới cho nhân viên và phiếu giảm giá
    private String tenNhanVien;
    private String maNhanVien;
    private String tenPhieuGiamGia;
    private String maPhieuGiamGia;
}
