package org.example.be_sp.model.response;

import java.time.LocalDate;

import org.example.be_sp.entity.AnhSanPham;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AnhSanPhamResponse {
    Integer id;
    String duongDanAnh;
    String tenAnh;
    String mauAnh;
    Boolean trangThai;
    Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;

    public AnhSanPhamResponse(AnhSanPham data) {
        this.id = data.getId();
        this.duongDanAnh = data.getDuongDanAnh();
        this.tenAnh = data.getTenAnh();
        this.mauAnh = data.getMauAnh();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
