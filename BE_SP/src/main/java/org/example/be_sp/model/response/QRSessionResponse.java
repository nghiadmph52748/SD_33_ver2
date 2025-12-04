package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRSessionResponse {
    private String qrSessionId;
    private String qrCodeUrl;
    private String orderCode;
    private List<CartItemResponse> items;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalPrice;
    private String status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
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


