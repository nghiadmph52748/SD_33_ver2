package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.DiaChiKhachHang;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DiaChiKhachHangResponse {
    Integer id;
    Integer idKhachHang;
    String maDiaChi;
    String tenDiaChi;
    String thanhPho;
    String quan;
    String phuong;
    String diaChiCuThe;
    Boolean deleted;

    public DiaChiKhachHangResponse(DiaChiKhachHang d) {
        this.id = d.getId();
        this.idKhachHang = d.getIdKhachHang().getId();
        this.maDiaChi = d.getMaDiaChi();
        this.tenDiaChi = d.getTenDiaChi();
        this.thanhPho = d.getThanhPho();
        this.quan = d.getQuan();
        this.phuong = d.getPhuong();
        this.diaChiCuThe = d.getDiaChiCuThe();
        this.deleted = d.getDeleted();
    }
}
