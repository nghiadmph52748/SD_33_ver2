package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.KichThuoc;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KichThuocResponse {
    Integer id;
    String maKichThuoc;
    String tenKichThuoc;
    Boolean trangThai;
    Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;

    public KichThuocResponse(KichThuoc data) {
        this.id = data.getId();
        this.maKichThuoc = data.getMaKichThuoc();
        this.tenKichThuoc = data.getTenKichThuoc();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
