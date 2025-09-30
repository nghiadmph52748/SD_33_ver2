package org.example.be_sp.model.response;

import org.example.be_sp.entity.NhaSanXuat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class NhaSanXuatResponse {
    private Integer id;
    private String maNhaSanXuat;
    private String tenNhaSanXuat;
    private Boolean trangThai;
    private Boolean deleted;
    private LocalDate createAt;
    private Integer createBy;
    private LocalDate updateAt;
    private Integer updateBy;

    public NhaSanXuatResponse(NhaSanXuat nhaSanXuat) {
        this.id = nhaSanXuat.getId();
        this.maNhaSanXuat = nhaSanXuat.getMaNhaSanXuat();
        this.tenNhaSanXuat = nhaSanXuat.getTenNhaSanXuat();
        this.trangThai = nhaSanXuat.getTrangThai();
        this.deleted = nhaSanXuat.getDeleted();
        this.createAt = nhaSanXuat.getCreateAt();
        this.createBy = nhaSanXuat.getCreateBy();
        this.updateAt = nhaSanXuat.getUpdateAt();
        this.updateBy = nhaSanXuat.getUpdateBy();
    }
}
