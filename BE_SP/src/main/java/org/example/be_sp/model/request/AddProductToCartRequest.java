package org.example.be_sp.model.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * Request to add product to cart (tạo hoá đơn chi tiết)
 */
@Getter
@Setter
public class AddProductToCartRequest {
	private Integer idHoaDon; // Cart invoice ID
	private Integer idBienThe; // Variant ID (idChiTietSanPham)
	private Integer soLuong; // Quantity
	private BigDecimal giaBan; // Unit price
	private Boolean trangThai; // Status
	private Boolean deleted; // Deleted flag
	private LocalDate createAt; // Creation date
}

