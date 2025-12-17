package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCustomerProfileRequest {
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    // Địa chỉ đơn giản cho khách hàng tự cập nhật
    private String thanhPho;
    private String quan;
    private String phuong;
    private String diaChiCuThe;
}

