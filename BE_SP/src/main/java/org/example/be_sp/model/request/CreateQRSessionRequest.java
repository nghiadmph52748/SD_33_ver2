package org.example.be_sp.model.request;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class CreateQRSessionRequest {

    private String orderCode;
    private Integer invoiceId;
    private List<CartItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalPrice;
    private BigDecimal transferAmount;
    private String customerId;

    @Data
    public static class CartItemDTO {

        private String productId;
        private String productName;
        private BigDecimal price;
        private BigDecimal discount;
        private Integer quantity;
        private String image;
        private String tenMauSac;
        private String maMau;
        private String tenKichThuoc;
        private String tenDeGiay;
        private String tenChatLieu;
    }
}
