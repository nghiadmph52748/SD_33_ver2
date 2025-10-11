package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ChiTietSanPham;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSanPhamFullResponse {

    private Integer id;
    private String maChiTietSanPham;
    private String tenSanPham;
    private String tenSanPhamChiTiet;
    private List<String> anhSanPham;
    private String tenNhaSanXuat;
    private String tenXuatXu;
    private String tenDeGiay;
    private String tenChatLieu;
    private String tenMauSac;
    private String tenKichThuoc;
    private String tenTrongLuong;
    private Integer soLuong;
    private BigDecimal giaBan;
    private Integer idDotGiamGia;
    private String tenDotGiamGia;
    private Integer giaTriGiamGia;
    private Boolean trangThai;
    private String ghiChu;
    private Boolean deleted;
    private LocalDate createdAt;
    private Integer createdBy;
    private LocalDate updatedAt;
    private Integer updatedBy;

    public ChiTietSanPhamFullResponse(ChiTietSanPham s) {
        this.id = s.getId();
        this.maChiTietSanPham = s.getMaChiTietSanPham();
        if (s.getIdSanPham() != null) {
            this.tenSanPham = s.getIdSanPham().getTenSanPham();
        } else {
            this.tenSanPham = null;
        }
        // Tạo tên sản phẩm chi tiết từ các thành phần
        StringBuilder tenSanPhamChiTietBuilder = new StringBuilder();
        if (s.getIdSanPham() != null && s.getIdSanPham().getTenSanPham() != null) {
            tenSanPhamChiTietBuilder.append(s.getIdSanPham().getTenSanPham());
        }
        if (s.getIdMauSac() != null && s.getIdMauSac().getTenMauSac() != null) {
            tenSanPhamChiTietBuilder.append(" - ").append(s.getIdMauSac().getTenMauSac());
        }
        if (s.getIdKichThuoc() != null && s.getIdKichThuoc().getTenKichThuoc() != null) {
            tenSanPhamChiTietBuilder.append(" - Size ").append(s.getIdKichThuoc().getTenKichThuoc());
        }
        this.tenSanPhamChiTiet = tenSanPhamChiTietBuilder.toString();
        if (s.getChiTietSanPhamAnhs() != null) {
            this.anhSanPham = s.getChiTietSanPhamAnhs().stream()
                    .filter(anh -> anh != null && anh.getIdAnhSanPham() != null
                    && Boolean.TRUE.equals(anh.getTrangThai()) && Boolean.FALSE.equals(anh.getDeleted()))
                    .map(anh -> anh.getIdAnhSanPham().getDuongDanAnh())
                    .filter(duongDan -> duongDan != null)
                    .collect(Collectors.toList());
        } else {
            this.anhSanPham = Collections.emptyList();
        }
        if (s.getIdSanPham() != null && s.getIdSanPham().getIdNhaSanXuat() != null) {
            this.tenNhaSanXuat = s.getIdSanPham().getIdNhaSanXuat().getTenNhaSanXuat();
        } else {
            this.tenNhaSanXuat = null;
        }
        if (s.getIdSanPham() != null && s.getIdSanPham().getIdXuatXu() != null) {
            this.tenXuatXu = s.getIdSanPham().getIdXuatXu().getTenXuatXu();
        } else {
            this.tenXuatXu = null;
        }
        if (s.getIdDeGiay() != null) {
            this.tenDeGiay = s.getIdDeGiay().getTenDeGiay();
        } else {
            this.tenDeGiay = null;
        }
        if (s.getIdChatLieu() != null) {
            this.tenChatLieu = s.getIdChatLieu().getTenChatLieu();
        } else {
            this.tenChatLieu = null;
        }
        if (s.getIdMauSac() != null) {
            this.tenMauSac = s.getIdMauSac().getTenMauSac();
        } else {
            this.tenMauSac = null;
        }
        if (s.getIdKichThuoc() != null) {
            this.tenKichThuoc = s.getIdKichThuoc().getTenKichThuoc();
        } else {
            this.tenKichThuoc = null;
        }
        if (s.getIdTrongLuong() != null) {
            this.tenTrongLuong = s.getIdTrongLuong().getTenTrongLuong();
        } else {
            this.tenTrongLuong = null;
        }
        this.soLuong = s.getSoLuong();
        this.giaBan = s.getGiaBan();
        this.trangThai = s.getTrangThai();
        if (s.getChiTietDotGiamGias() != null) {
            this.tenDotGiamGia = s.getChiTietDotGiamGias().stream()
                    .filter(ct -> ct != null && ct.getIdDotGiamGia() != null && Boolean.FALSE.equals(ct.getDeleted())
                    && Boolean.TRUE.equals(ct.getTrangThai()))
                    .map(ct -> ct.getIdDotGiamGia().getTenDotGiamGia())
                    .filter(ten -> ten != null)
                    .findFirst()
                    .orElse(null);
            this.giaTriGiamGia = s.getChiTietDotGiamGias().stream()
                    .filter(ct -> ct != null && ct.getIdDotGiamGia() != null && Boolean.FALSE.equals(ct.getDeleted())
                    && Boolean.TRUE.equals(ct.getTrangThai()))
                    .map(ct -> ct.getIdDotGiamGia().getGiaTriGiamGia())
                    .filter(giaTri -> giaTri != null)
                    .findFirst()
                    .orElse(null);
        } else {
            this.tenDotGiamGia = null;
            this.giaTriGiamGia = null;
        }
        if (s.getChiTietDotGiamGias() != null) {
            this.idDotGiamGia = s.getChiTietDotGiamGias().stream()
                    .filter(ct -> ct != null && ct.getIdDotGiamGia() != null && Boolean.FALSE.equals(ct.getDeleted())
                    && Boolean.TRUE.equals(ct.getTrangThai()))
                    .map(ct -> ct.getIdDotGiamGia().getId())
                    .filter(idValue -> idValue != null)
                    .findFirst()
                    .orElse(null);
        } else {
            this.idDotGiamGia = null;
        }
        this.ghiChu = s.getGhiChu();
        this.deleted = s.getDeleted();
        this.createdAt = s.getCreateAt();
        this.createdBy = s.getCreateBy();
        this.updatedAt = s.getUpdateAt();
        this.updatedBy = s.getUpdateBy();
    }
}
