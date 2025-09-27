package org.example.be_sp.model.response;

import org.example.be_sp.entity.ChiTietSanPhamAnh;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietSanPhamAnhResponse {
    Integer id;
    Integer idChiTietSanPham;
    Integer idAnhSanPham;
    Boolean deleted;

    public ChiTietSanPhamAnhResponse(ChiTietSanPhamAnh d) {
        this.id = d.getId();
        this.idChiTietSanPham = d.getIdChiTietSanPham() != null ? d.getIdChiTietSanPham().getId() : null;
        this.idAnhSanPham = d.getIdAnhSanPham() != null ? d.getIdAnhSanPham().getId() : null;
        this.deleted = d.getDeleted();
    }
}
