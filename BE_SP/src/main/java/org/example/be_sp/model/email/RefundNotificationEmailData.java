package org.example.be_sp.model.email;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundNotificationEmailData {
    private String customerName;
    private String customerEmail;
    private String orderCode;
    private BigDecimal refundAmount;
    private boolean appliedToOrder;
    private BigDecimal originalTotal;
    private BigDecimal newTotal;
    private boolean giftReward;
    private boolean voucherReward;
    private String voucherCode;
    private LocalDateTime voucherExpiry;
}
