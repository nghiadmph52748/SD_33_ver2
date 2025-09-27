package org.example.be_sp.model.response;

import java.time.LocalDate;

import org.example.be_sp.entity.XuatXu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class XuatXuResponse {
    Integer id;
    String maXuatXu;
    String tenXuatXu;
    Boolean trangThai;
    Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;

    public XuatXuResponse(XuatXu d) {
        this.id = d.getId();
        this.maXuatXu = d.getMaXuatXu();
        this.tenXuatXu = d.getTenXuatXu();
        this.deleted = d.getDeleted();
        this.trangThai = d.getTrangThai();
        this.createAt = d.getCreateAt();
        this.createBy = d.getCreateBy();
        this.updateAt = d.getUpdateAt();
        this.updateBy = d.getUpdateBy();
    }
}
