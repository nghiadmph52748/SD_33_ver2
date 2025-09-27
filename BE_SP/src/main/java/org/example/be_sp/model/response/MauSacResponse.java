package org.example.be_sp.model.response;

import java.time.LocalDate;

import org.example.be_sp.entity.MauSac;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MauSacResponse {
    Integer id;
    String maMauSac;
    String tenMauSac;
    String maMau;
    Boolean deleted;
    Boolean trangThai;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;

    public MauSacResponse(MauSac data) {
        this.id = data.getId();
        this.maMauSac = data.getMaMauSac();
        this.tenMauSac = data.getTenMauSac();
        this.maMau = data.getMaMau();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
