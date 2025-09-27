package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NhanVienRequest {
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
}
