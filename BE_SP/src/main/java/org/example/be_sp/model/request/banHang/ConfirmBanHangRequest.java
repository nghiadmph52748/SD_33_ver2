package org.example.be_sp.model.request.banHang;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class ConfirmBanHangRequest {
    Integer idHoaDon;
    String maHoaDon;
    Map<Integer, Integer> danhSachSanPham; // key: idChiTietSanPham, value: soLuong
    Integer idPhieuGiamGia; // null nếu không sử dụng
    Integer idNhanVien;
    Integer idKhachHang; // null nếu không có
    Integer idHinhThucThanhToan;
    Integer idPTTT; // 1: tiền mặt, 2: chuyển khoản, 3: cả 2
    Boolean loaiDon; // true: tại quầy, false: giao hàng
    BigDecimal phiVanChuyen; // null nếu không có
    BigDecimal tongTien;
    BigDecimal tongTienSauGiam;
    String tenKhachHang; // null nếu có trong hệ thống
    String soDienThoai; // null nếu có trong hệ thống
    String diaChiKhachHang; // null nếu là đơn tại quầy || null nếu có trong hệ thống
    String emailKhachHang; // null nếu có trong hệ thống
    Boolean trangThaiThanhToan; // true: đã thanh toán, false: chưa thanh toán
    BigDecimal tienMat;
    BigDecimal tienChuyenKhoan;
    BigDecimal soTienConLai;
}
