package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.util.List;

import org.example.be_sp.entity.HoaDonChiTiet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HoaDonChiTietResponse {

    private Integer id;
    private String idmaKhachHang;
    private String idtenKhachHang;
    private String idmaPhieuGiamGia;
    private String idtenPhieuGiamGia;
    private String idmaNhanVien;
    private String idtenNhanVien;
    private String maHoaDon;
    private String tenHoaDon;
    private Boolean loaiDon;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTien;
    private BigDecimal tongTienSauGiam;
    private String tenNguoiNhan;
    private String diaChiNguoiNhan;
    private String sdtNguoiNhan;
    private String emailNguoiNhan;
    private String maSanPham;
    private String tenSanPham;
    private String maMauSac;
    private String tenMauSac;
    private String maKichThuoc;
    private String tenKichThuoc;
    private String maDeGiay;
    private String tenDeGiay;
    private String maChatLieu;
    private String tenChatLieu;
    private String maDemGiay;
    private String tenDemGiay;
    private String maTrongLuong;
    private String tenTrongLuong;
    private String maMonTheThao;
    private String tenMonTheThao;
    private String maLoaiMua;
    private String tenLoaiMua;
    private String maDoBen;
    private String tenDoBen;
    private String maChongNuoc;
    private String tenChongNuoc;
    private Integer soLuong;
    private BigDecimal giaBan;
    private String maHoaDonChiTiet;
    private BigDecimal thanhTien;
    private Boolean trangThai;
    private String ghiChu;
    private String tenSanPhamChiTiet;
    private String maSanPhamChiTiet;
    private Boolean deleted;
    private Integer sanPhamId;
    private String tenNhaSanXuat;
    private BigDecimal giaBanSanPham;
    private List<String> anhSanPham;
    private ChiTietSanPhamFullResponse sanPham;

    public HoaDonChiTietResponse(HoaDonChiTiet d) {
        this.id = d.getId();

        // Hóa đơn thông tin
        if (d.getIdHoaDon() != null) {
            this.maHoaDon = d.getIdHoaDon().getMaHoaDon();
            this.loaiDon = d.getIdHoaDon().getGiaoHang();
            this.phiVanChuyen = d.getIdHoaDon().getPhiVanChuyen();
            this.tongTien = d.getIdHoaDon().getTongTien();
            this.tongTienSauGiam = d.getIdHoaDon().getTongTienSauGiam();
            this.tenNguoiNhan = d.getIdHoaDon().getTenNguoiNhan();
            this.diaChiNguoiNhan = d.getIdHoaDon().getDiaChiNguoiNhan();
            this.sdtNguoiNhan = d.getIdHoaDon().getSoDienThoaiNguoiNhan();
            this.emailNguoiNhan = d.getIdHoaDon().getEmailNguoiNhan();

            // Khách hàng
            if (d.getIdHoaDon().getIdKhachHang() != null) {
                this.idmaKhachHang = d.getIdHoaDon().getIdKhachHang().getMaKhachHang();
                this.idtenKhachHang = d.getIdHoaDon().getIdKhachHang().getTenKhachHang();
            }

            // Phiếu giảm giá
            if (d.getIdHoaDon().getIdPhieuGiamGia() != null) {
                this.idmaPhieuGiamGia = d.getIdHoaDon().getIdPhieuGiamGia().getMaPhieuGiamGia();
                this.idtenPhieuGiamGia = d.getIdHoaDon().getIdPhieuGiamGia().getTenPhieuGiamGia();
            }

            // Nhân viên
            if (d.getIdHoaDon().getIdNhanVien() != null) {
                this.idmaNhanVien = d.getIdHoaDon().getIdNhanVien().getMaNhanVien();
                this.idtenNhanVien = d.getIdHoaDon().getIdNhanVien().getTenNhanVien();
            }
        }

        // Chi tiết sản phẩm - lấy tên từ ghi chú
        if (d.getIdChiTietSanPham() != null) {
            if (d.getIdChiTietSanPham().getIdSanPham() != null) {
                this.tenSanPham = d.getIdChiTietSanPham().getIdSanPham().getTenSanPham();
                this.maSanPham = d.getIdChiTietSanPham().getIdSanPham().getMaSanPham();
            }

            // Lấy thông tin màu sắc
            if (d.getIdChiTietSanPham().getIdMauSac() != null) {
                this.tenMauSac = d.getIdChiTietSanPham().getIdMauSac().getTenMauSac();
                this.maMauSac = d.getIdChiTietSanPham().getIdMauSac().getMaMauSac();
            }

            // Lấy thông tin kích thước
            if (d.getIdChiTietSanPham().getIdKichThuoc() != null) {
                this.tenKichThuoc = d.getIdChiTietSanPham().getIdKichThuoc().getTenKichThuoc();
                this.maKichThuoc = d.getIdChiTietSanPham().getIdKichThuoc().getMaKichThuoc();
            }
        }

        // Ưu tiên lấy tên từ ghi chú nếu có
        if (d.getGhiChu() != null && !d.getGhiChu().trim().isEmpty()) {
            this.tenSanPham = d.getGhiChu();
        }

        // Thông tin chi tiết hóa đơn
        this.soLuong = d.getSoLuong();
        this.giaBan = d.getGiaBan();
        this.maHoaDonChiTiet = d.getMaHoaDonChiTiet();
        this.thanhTien = d.getThanhTien();
        this.trangThai = d.getTrangThai();
        this.ghiChu = d.getGhiChu();
        this.tenSanPhamChiTiet = d.getTenSanPhamChiTiet();
        this.maSanPhamChiTiet = d.getMaSanPhamChiTiet();
        this.deleted = d.getDeleted();
//        if (d.getIdChiTietSanPham() != null) {
//            this.sanPham = new ChiTietSanPhamFullResponse(d.getIdChiTietSanPham());
//        }

    }

}
