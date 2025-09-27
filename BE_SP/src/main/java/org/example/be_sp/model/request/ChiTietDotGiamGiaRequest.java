package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChiTietDotGiamGiaRequest {
    Integer idDotGiamGia;
    Integer idChiTietSanPham;
    Boolean deleted;
}
