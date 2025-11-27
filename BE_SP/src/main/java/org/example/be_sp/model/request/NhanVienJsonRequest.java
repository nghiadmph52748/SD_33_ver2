package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienJsonRequest {
    private String tenNhanVien;
    private String tenTaiKhoan;
    private String matKhau;
    private String email;
    private String soDienThoai;
    private String anhNhanVien;
    private LocalDate ngaySinh;
    private String thanhPho;
    private String quan;
    private String phuong;
    private String diaChiCuThe;
    private String cccd;
    private Boolean gioiTinh;
    private Integer idQuyenHan;
    private Boolean trangThai;
    private Boolean deleted;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;
    
    public NhanVienRequest toNhanVienRequest() {
        NhanVienRequest request = new NhanVienRequest();
        request.setTenNhanVien(this.tenNhanVien);
        request.setTenTaiKhoan(this.tenTaiKhoan);
        request.setMatKhau(this.matKhau);
        request.setEmail(this.email);
        request.setSoDienThoai(this.soDienThoai);
        request.setAnhNhanVienUrl(this.anhNhanVien);
        request.setNgaySinh(this.ngaySinh);
        request.setThanhPho(this.thanhPho);
        request.setQuan(this.quan);
        request.setPhuong(this.phuong);
        request.setDiaChiCuThe(this.diaChiCuThe);
        request.setCccd(this.cccd);
        request.setGioiTinh(this.gioiTinh);
        request.setIdQuyenHan(this.idQuyenHan);
        request.setTrangThai(this.trangThai);
        request.setDeleted(this.deleted);
        request.setCreateAt(this.createAt);
        request.setCreateBy(this.createBy);
        request.setUpdateAt(this.updateAt);
        request.setUpdateBy(this.updateBy);
        return request;
    }
}

