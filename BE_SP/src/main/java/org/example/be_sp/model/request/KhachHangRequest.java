package org.example.be_sp.model.request;

import jdk.dynalink.linker.LinkerServices;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.model.DiaChi;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

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
}
