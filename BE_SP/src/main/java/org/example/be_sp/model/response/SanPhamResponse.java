package org.example.be_sp.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.example.be_sp.entity.SanPham;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SanPhamResponse {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private Integer soLuongBienThe;
    private BigDecimal giaNhoNhat;
    private BigDecimal giaLonNhat;
    private Boolean trangThai;
    private Boolean deleted;
    private Integer idNhaSanXuat;
    private Integer idXuatXu;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;

    public SanPhamResponse(SanPham sp) {
        this.id = sp.getId();
        this.maSanPham = sp.getMaSanPham();
        this.tenSanPham = sp.getTenSanPham();
        this.soLuongBienThe = sp.getChiTietSanPhams().size();
        List<BigDecimal> giaBan = sp.getChiTietSanPhams().stream().filter(ct -> ct.getGiaBan() != null).map(ct -> ct.getGiaBan()).toList();
        this.giaNhoNhat = giaBan.stream().min(BigDecimal::compareTo).orElse(null);
        this.giaLonNhat = giaBan.stream().max(BigDecimal::compareTo).orElse(null);
        this.trangThai = sp.getTrangThai();
        this.deleted = sp.getDeleted();
        this.idNhaSanXuat = sp.getIdNhaSanXuat().getId();
        this.idXuatXu = sp.getIdXuatXu().getId();
        this.createAt = sp.getCreateAt();
        this.createBy = sp.getCreateBy();
        this.updateAt = sp.getUpdateAt();
        this.updateBy = sp.getUpdateBy();
    }
}
