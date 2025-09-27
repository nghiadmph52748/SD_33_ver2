package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.DotGiamGia;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DotGiamGiaResponse {
    Integer id;
    String maDotGiamGia;
    String tenDotGiamGia;
    Integer giaTriGiamGia;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    Boolean trangThai;
    Boolean deleted;

    public DotGiamGiaResponse(DotGiamGia d) {
        this.id = d.getId();
        this.maDotGiamGia = d.getMaDotGiamGia();
        this.tenDotGiamGia = d.getTenDotGiamGia();
        this.giaTriGiamGia = d.getGiaTriGiamGia();
        this.ngayBatDau = d.getNgayBatDau();
        this.ngayKetThuc = d.getNgayKetThuc();
        this.trangThai = d.getTrangThai();
        this.deleted = d.getDeleted();
    }
}
