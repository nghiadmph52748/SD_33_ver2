package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QRSessionEvent {

    private String sessionId;
    private String orderCode;
    private String status;
    private BigDecimal finalPrice;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private String qrCodeUrl;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
