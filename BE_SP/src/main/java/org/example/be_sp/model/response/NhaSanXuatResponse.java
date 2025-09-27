package org.example.be_sp.model.response;

import org.example.be_sp.entity.NhaSanXuat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NhaSanXuatResponse {
    private Integer id;
    private String maNhaSanXuat;
    private String tenNhaSanXuat;
    private Boolean trangThai;
    private Boolean deleted;

    public NhaSanXuatResponse(NhaSanXuat nhaSanXuat) {
        this.id = nhaSanXuat.getId();
        this.maNhaSanXuat = nhaSanXuat.getMaNhaSanXuat();
        this.tenNhaSanXuat = nhaSanXuat.getTenNhaSanXuat();
        this.trangThai = nhaSanXuat.getTrangThai();
        this.deleted = nhaSanXuat.getDeleted();
    }
}
