package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerLoginResponse {
    private String accessToken;
    private String refreshToken;
    private CustomerProfileResponse khachHang;

    public CustomerLoginResponse(String accessToken, String refreshToken, CustomerProfileResponse profile) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.khachHang = profile;
    }
}


