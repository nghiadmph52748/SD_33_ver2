package org.example.be_sp.model.response;

import org.example.be_sp.entity.KhachHang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerProfileResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String tenTaiKhoan;
    private String email;
    private String soDienThoai;

    public CustomerProfileResponse() {
    }

    public CustomerProfileResponse(KhachHang entity) {
        if (entity == null) {
            return;
        }
        this.id = entity.getId();
        this.maKhachHang = entity.getMaKhachHang();
        this.tenKhachHang = entity.getTenKhachHang();
        this.tenTaiKhoan = entity.getTenTaiKhoan();
        this.email = entity.getEmail();
        this.soDienThoai = entity.getSoDienThoai();
    }
}


