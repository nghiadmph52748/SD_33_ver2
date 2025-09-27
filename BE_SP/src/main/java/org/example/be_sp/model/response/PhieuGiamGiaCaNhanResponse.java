package org.example.be_sp.model.response;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.PhieuGiamGiaCaNhan;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaCaNhanResponse {
    private Integer id;
    private String maPhieuGiamGiaCaNhan;
    private String tenPhieuGiamGiaCaNhan;
    private String tenKhachHang;
    String email;
    private String tenPhieuGiamGia;
    Boolean loaiPhieuGiamGia; // true la phan tram, false la so tien
    BigDecimal giaTriGiamGia;
    private BigDecimal hoaDonToiThieu;
    private BigDecimal soTienToiDa;
    Integer soLuongSuDung;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    private LocalDate ngayNhan;
    private LocalDate ngayHetHan;
    private Boolean trangThai;
    Boolean deleted;

    public PhieuGiamGiaCaNhanResponse(PhieuGiamGiaCaNhan r) {
        this.id = r.getId();
        this.maPhieuGiamGiaCaNhan = r.getMaPhieuGiamGiaCaNhan();
        this.tenPhieuGiamGiaCaNhan = r.getTenPhieuGiamGiaCaNhan();
        this.tenKhachHang = r.getIdKhachHang().getTenKhachHang();
        this.email = r.getIdKhachHang().getEmail();
        this.tenPhieuGiamGia = r.getIdPhieuGiamGia().getTenPhieuGiamGia();
        this.loaiPhieuGiamGia = r.getIdPhieuGiamGia().getLoaiPhieuGiamGia();
        this.giaTriGiamGia = r.getIdPhieuGiamGia().getGiaTriGiamGia();
        this.hoaDonToiThieu = r.getIdPhieuGiamGia().getHoaDonToiThieu();
        this.soTienToiDa = r.getIdPhieuGiamGia().getSoTienToiDa();
        this.soLuongSuDung = r.getIdPhieuGiamGia().getSoLuongDung();
        this.ngayBatDau = r.getIdPhieuGiamGia().getNgayBatDau();
        this.ngayKetThuc = r.getIdPhieuGiamGia().getNgayKetThuc();
        this.ngayNhan = r.getNgayNhan();
        this.ngayHetHan = r.getNgayHetHan();
        this.trangThai = r.getTrangThai();
        this.deleted = r.getDeleted();
    }
}
