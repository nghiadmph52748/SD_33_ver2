package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.example.be_sp.entity.ChiTietSanPham;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.PropertyValue;

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
	private String maMau;
	private String tenKichThuoc;
	private String tenTrongLuong;
	private Integer soLuong;
	private BigDecimal giaBan;
	private Integer idDotGiamGia;
	private String tenDotGiamGia;
	private Integer giaTriGiamGia;
	private String qrcode;
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
		if (s.getIdMauSac() != null) {
			this.maMau = s.getIdMauSac().getMaMau();
		} else {
			this.maMau = null;
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
		this.qrcode = s.getQrcode();
		this.trangThai = s.getTrangThai();

		// Lọc đợt khuyến mãi còn hiệu lực và chọn đợt có giá trị giảm cao nhất
		if (s.getChiTietDotGiamGias() != null) {
			LocalDateTime now = LocalDateTime.now();

			var validPromotion = s.getChiTietDotGiamGias().stream()
					.filter(ct -> ct != null
							&& ct.getIdDotGiamGia() != null
							&& Boolean.FALSE.equals(ct.getDeleted())
							&& Boolean.TRUE.equals(ct.getTrangThai())
							// Kiểm tra đợt giảm giá còn hoạt động
							&& Boolean.FALSE.equals(ct.getIdDotGiamGia().getDeleted())
							&& Boolean.TRUE.equals(ct.getIdDotGiamGia().getTrangThai())
							// Kiểm tra thời gian hiệu lực
							&& (ct.getIdDotGiamGia().getNgayBatDau() == null
									|| !now.isBefore(ct.getIdDotGiamGia().getNgayBatDau()))
							&& (ct.getIdDotGiamGia().getNgayKetThuc() == null
									|| !now.isAfter(ct.getIdDotGiamGia().getNgayKetThuc())))
					// Sắp xếp theo giá trị giảm giá giảm dần (cao nhất trước)
					.max((ct1, ct2) -> {
						Integer discount1 = ct1.getIdDotGiamGia().getGiaTriGiamGia();
						Integer discount2 = ct2.getIdDotGiamGia().getGiaTriGiamGia();
						if (discount1 == null)
							return -1;
						if (discount2 == null)
							return 1;
						return discount1.compareTo(discount2);
					});

			this.tenDotGiamGia = validPromotion
					.map(ct -> ct.getIdDotGiamGia().getTenDotGiamGia())
					.orElse(null);
			this.giaTriGiamGia = validPromotion
					.map(ct -> ct.getIdDotGiamGia().getGiaTriGiamGia())
					.orElse(null);
			this.idDotGiamGia = validPromotion
					.map(ct -> ct.getIdDotGiamGia().getId())
					.orElse(null);
		} else {
			this.tenDotGiamGia = null;
			this.giaTriGiamGia = null;
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
