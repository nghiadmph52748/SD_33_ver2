package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VnpayCreatePaymentResponse {
    private String payUrl;
    private String txnRef;
}
