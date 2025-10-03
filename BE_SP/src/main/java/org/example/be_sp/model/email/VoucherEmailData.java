package org.example.be_sp.model.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Email data transfer object for voucher assignment emails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherEmailData {
    private String customerName;
    private String customerEmail;
    private String voucherCode;
    private String voucherName;
    private String voucherType; // "PERCENTAGE" or "FIXED_AMOUNT"
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private LocalDate validFrom;
    private LocalDate validUntil;
    private Integer usageLimit;
    private String description;
}
