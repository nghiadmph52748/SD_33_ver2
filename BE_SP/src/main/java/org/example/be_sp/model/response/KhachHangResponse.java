package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.model.DiaChi;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String tenKhachHang;
    private String tenTaiKhoan;
    private String matKhau;
    private String email;
    private String soDienThoai;
    private Boolean gioiTinh;
    private LocalDate ngaySinh;
    private Integer phanLoai;
    private Boolean deleted;

    private List<DiaChi> listDiaChi;
    public KhachHangResponse(KhachHang e) {
        this.id = e.getId();
        this.maKhachHang = e.getMaKhachHang();
        this.tenKhachHang = e.getTenKhachHang();
        this.email = e.getEmail();
        this.tenTaiKhoan = e.getTenTaiKhoan();
        this.matKhau = e.getMatKhau();
        this.soDienThoai = e.getSoDienThoai();
        this.gioiTinh = e.getGioiTinh();
        this.ngaySinh = e.getNgaySinh();
        this.phanLoai = e.getPhanLoai();
        this.deleted = e.getDeleted();
        this.listDiaChi = Optional.ofNullable(e.getDiaChiKhachHangs())
                .orElseGet(Collections::emptySet)
                .stream()
                .filter(diaChi -> !Boolean.TRUE.equals(diaChi.getDeleted()))
                .map(diaChi -> new DiaChi(
                        diaChi.getDiaChiCuThe(),
                        diaChi.getThanhPho(),
                        diaChi.getQuan(),
                        diaChi.getPhuong()
                ))
                .toList();
    }
}
