package org.example.be_sp.model.request;

import lombok.Data;

@Data
public class MoMoCreatePaymentRequest {
    private Long amount;
    private String orderId;
    private String orderInfo;
}









