package org.example.be_sp.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Email data transfer object for order-related emails
 * Used for both order confirmation and status update emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEmailData {
    private String orderCode;
    private String customerName;
    private String customerEmail;
    private LocalDate orderDate;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal finalAmount;
    private String orderStatus;
    private String deliveryAddress;
    private String phoneNumber;
    private String trackingUrl;
    private List<OrderItemData> items;
    
    /**
     * Nested class for individual order items
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemData {
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }
}
