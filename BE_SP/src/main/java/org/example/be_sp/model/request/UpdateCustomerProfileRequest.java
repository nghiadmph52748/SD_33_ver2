package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest {
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
}

