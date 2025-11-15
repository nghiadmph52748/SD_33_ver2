package org.example.be_sp.model.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response when adding product to cart
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddProductToCartResponse {

    private Integer idHoaDon;               // Cart invoice ID
    private Integer idChiTietHoaDon;        // Invoice detail ID
    private Integer soLuong;                // Quantity
    private BigDecimal giaBan;              // Unit price
    private BigDecimal thanhTien;           // Total amount (quantity * price)
}
