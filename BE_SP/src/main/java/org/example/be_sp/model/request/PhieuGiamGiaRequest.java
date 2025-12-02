package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaRequest {
    String maPhieuGiamGia;
    String tenPhieuGiamGia;
    Boolean loaiPhieuGiamGia;
    BigDecimal giaTriGiamGia;
    BigDecimal soTienToiDa;
    BigDecimal hoaDonToiThieu;
    List<Integer> idKhachHang;
    Integer soLuongDung;
    LocalDateTime ngayBatDau;
    LocalDateTime ngayKetThuc;
    Boolean trangThai;
    String moTa;
    Boolean deleted;
    Boolean featured;
    String lyDoThayDoi;
}
