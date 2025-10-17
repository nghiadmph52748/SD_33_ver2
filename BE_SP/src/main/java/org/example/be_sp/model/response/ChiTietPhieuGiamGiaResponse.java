package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.ChiTietPhieuGiamGia;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietPhieuGiamGiaResponse {
    Integer id;
    Integer idPhieuGiamGia;
    Integer idChiTietSanPham;
    String tenChiTietSanPham;
    String maChiTietSanPham;
    Boolean deleted;

    public ChiTietPhieuGiamGiaResponse(ChiTietPhieuGiamGia d) {
        this.id = d.getId();
        this.idPhieuGiamGia = d.getIdPhieuGiamGia().getId();
        this.idChiTietSanPham = d.getIdChiTietSanPham().getId();
        this.tenChiTietSanPham = d.getIdChiTietSanPham().getTenChiTietSanPham();
        this.maChiTietSanPham = d.getIdChiTietSanPham().getMaChiTietSanPham();
        this.deleted = d.getDeleted();
    }
}

