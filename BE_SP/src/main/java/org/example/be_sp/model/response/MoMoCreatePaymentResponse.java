package org.example.be_sp.model.response;

import lombok.Data;

@Data
public class MoMoCreatePaymentResponse {
    private String payUrl;
    private String qrCodeUrl;
    private String deeplink;
    private String orderId;
    private String requestId;
}

