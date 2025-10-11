package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HoaDonRequest {
    private String tenHoaDon;
    private Boolean loaiDon;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTien;
    private BigDecimal tongTienSauGiam;
    private String ghiChu;
    private String tenNguoiNhan;
    private String diaChiNhanHang;
    private String soDienThoaiNguoiNhan;
    private String emailNguoiNhan;
    private String tenNhanVien;
    private String maNhanVien;
    private String tenPhieuGiamGia;
    private String maPhieuGiamGia;
    private LocalDate ngayThanhToan;
    private LocalDate ngayTao;
    private Integer idKhachHang;     // chỉ cần id thay vì full object
    private Integer idNhanVien;      // id nhân viên tạo/duyệt
    private Integer idPhieuGiamGia;  // id phiếu giảm giá (nếu có)

    private Boolean trangThai;
}