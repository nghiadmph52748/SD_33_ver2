package org.example.be_sp.model.response.invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response payload describing the synchronised payment summary for an invoice.
 */
@Getter
@AllArgsConstructor
public class InvoicePaymentSyncResponse {

    private final Integer invoiceId;
    private final BigDecimal totalAmount;
    private final BigDecimal paidAmount;
    private final BigDecimal remainingAmount;
    private final boolean fullyPaid;
    private final LocalDateTime syncedAt;
}
