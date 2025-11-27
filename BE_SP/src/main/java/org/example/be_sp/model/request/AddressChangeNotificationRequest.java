package org.example.be_sp.model.request;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddressChangeNotificationRequest {
    
    private AddressInfo oldAddress;
    private AddressInfo newAddress;
    private BigDecimal surcharge; // Keep for backward compatibility
    private ShippingFeeChange shippingFeeChange; // New: detailed fee change info

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressInfo {
        private String thanhPho;
        private String quan;
        private String phuong;
        private String diaChiCuThe;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShippingFeeChange {
        private BigDecimal currentFee; // Phí giao hàng cũ
        private BigDecimal newFee; // Phí giao hàng mới
        private BigDecimal difference; // Chênh lệch (âm = hoàn phí, dương = phụ phí)
        private Boolean isExtra; // true = phụ phí, false = hoàn phí
        private String description; // Mô tả chi tiết
    }
}

