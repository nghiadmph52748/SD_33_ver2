package org.example.be_sp.model.request;

import java.time.LocalDate;
import java.util.List;

import org.example.be_sp.model.DiaChi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangRequest {
    private String tenKhachHang;
    private String tenTaiKhoan;
    private String matKhau;
    private String email;
    private String soDienThoai;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private Boolean deleted;
    private List<DiaChi> listDiaChi;
    private Boolean trangThai;
	 private Integer phanLoai;
	 private LocalDate createAt;
	 private Integer createBy;
	 private LocalDate updateAt;
	 private Integer updateBy;
}
