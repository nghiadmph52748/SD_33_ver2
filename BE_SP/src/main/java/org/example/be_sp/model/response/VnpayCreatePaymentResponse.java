package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VnpayCreatePaymentResponse {
    private String payUrl;
    private String txnRef;
    private String qrCodeUrl; // VNPAY banking QR code image URL
}
