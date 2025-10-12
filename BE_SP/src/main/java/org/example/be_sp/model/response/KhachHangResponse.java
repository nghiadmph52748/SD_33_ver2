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
import java.util.stream.Collectors;

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
    private Integer tongDon;
    private Long tongChiTieu;
    private String trangThaiText;
    private String phanLoaiText;

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

        // Địa chỉ
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
                .collect(Collectors.toList());

        // Tổng đơn và tổng chi tiêu
        this.tongDon = e.getHoaDons() != null ? e.getHoaDons().size() : 0;

        this.tongChiTieu = Optional.ofNullable(e.getHoaDons())
                .orElse(Collections.emptySet())
                .stream()
                .filter(hd -> !Boolean.TRUE.equals(hd.getDeleted()))
                .mapToLong(hd -> hd.getTongTien() != null ? hd.getTongTien().longValue() : 0L)
                .sum();

        // Trạng thái
        this.trangThaiText = e.getTrangThai() != null && e.getTrangThai() ? "Hoạt động" : "Không hoạt động";

        // Phân loại
        this.phanLoaiText = Optional.ofNullable(e.getPhanLoai())
                .map(phanLoai -> switch (phanLoai) {
                    case 0 -> "Mới";
                    case 1 -> "Thường xuyên";
                    case 2 -> "VIP";
                    default -> "Không xác định";
                })
                .orElse("Không xác định");

    }
}
