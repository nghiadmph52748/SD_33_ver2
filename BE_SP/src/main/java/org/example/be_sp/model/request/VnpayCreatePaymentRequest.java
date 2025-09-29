package org.example.be_sp.model.request;

import lombok.Data;

@Data
public class VnpayCreatePaymentRequest {
    private Long amount;
    private String orderId;
    private String orderInfo;
    private String bankCode;
    private String locale;
}
