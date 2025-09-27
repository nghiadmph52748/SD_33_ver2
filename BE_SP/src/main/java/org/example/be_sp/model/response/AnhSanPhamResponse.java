package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.example.be_sp.entity.AnhSanPham;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class AnhSanPhamResponse {
    Integer id;
    String duongDanAnh;
    String loaiAnh;
    String moTa;
    Boolean trangThai;
    Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;

    public AnhSanPhamResponse(AnhSanPham data) {
        this.id = data.getId();
        this.duongDanAnh = data.getDuongDanAnh();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
