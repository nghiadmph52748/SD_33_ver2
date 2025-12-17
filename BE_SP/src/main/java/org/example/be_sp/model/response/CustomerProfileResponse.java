package org.example.be_sp.model.response;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.model.DiaChi;

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
    // Địa chỉ của khách hàng (dùng cho frontend checkout/profile)
    private List<DiaChi> listDiaChi;

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

        // Map danh sách địa chỉ giống KhachHangResponse
        this.listDiaChi = Optional.ofNullable(entity.getDiaChiKhachHangs())
                .orElseGet(Collections::emptySet)
                .stream()
                .filter(diaChi -> !Boolean.TRUE.equals(diaChi.getDeleted()))
                .map(diaChi -> new DiaChi(
                        diaChi.getTenDiaChi(),
                        diaChi.getDiaChiCuThe(),
                        diaChi.getThanhPho(),
                        diaChi.getQuan(),
                        diaChi.getPhuong(),
                        diaChi.getMacDinh()
                ))
                .collect(Collectors.toList());
    }
}


