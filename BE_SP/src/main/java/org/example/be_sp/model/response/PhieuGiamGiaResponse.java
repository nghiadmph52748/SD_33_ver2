package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.example.be_sp.entity.PhieuGiamGia;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaResponse {
    Integer id;
    String maPhieuGiamGia;
    String tenPhieuGiamGia;
    Boolean loaiPhieuGiamGia;
    BigDecimal giaTriGiamGia;
    BigDecimal hoaDonToiThieu;
    List<Integer> idKhachHang;
    Integer soLuongDung;
    Integer soLuongDaDung;
    LocalDateTime ngayBatDau;
    LocalDateTime ngayKetThuc;
    Boolean trangThai;
    String moTa;
    Boolean deleted;
    Boolean featured;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public PhieuGiamGiaResponse(PhieuGiamGia d) {
        this.id = d.getId();
        this.maPhieuGiamGia = d.getMaPhieuGiamGia();
        this.tenPhieuGiamGia = d.getTenPhieuGiamGia();
        this.loaiPhieuGiamGia = d.getLoaiPhieuGiamGia();
        this.giaTriGiamGia = d.getGiaTriGiamGia();
        this.idKhachHang = (d.getPhieuGiamGiaCaNhans() != null && !d.getPhieuGiamGiaCaNhans().isEmpty())
                ? d.getPhieuGiamGiaCaNhans().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getDeleted()))
                .map(p -> p.getIdKhachHang().getId())
                .toList()
                : List.of();
        this.hoaDonToiThieu = d.getHoaDonToiThieu();
        this.soLuongDung = d.getSoLuongDung();
        this.ngayBatDau = d.getNgayBatDau();
        this.ngayKetThuc = d.getNgayKetThuc();
        this.trangThai = d.getTrangThai();
        this.moTa = d.getMoTa();
        this.deleted = d.getDeleted();
        this.featured = d.getFeatured();
        this.createdAt = d.getCreatedAt();
        this.updatedAt = d.getUpdatedAt();
    }
}
