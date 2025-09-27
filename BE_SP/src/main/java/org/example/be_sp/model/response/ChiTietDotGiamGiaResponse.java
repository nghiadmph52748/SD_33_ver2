package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.ChiTietDotGiamGia;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietDotGiamGiaResponse {
    Integer id;
    Integer idDotGiamGia;
    Integer idChiTietSanPham;
    Boolean deleted;

    public ChiTietDotGiamGiaResponse(ChiTietDotGiamGia d) {
        this.id = d.getId();
        this.idDotGiamGia = d.getIdDotGiamGia().getId();
        this.idChiTietSanPham = d.getIdChiTietSanPham().getId();
        this.deleted = d.getDeleted();
    }
}
