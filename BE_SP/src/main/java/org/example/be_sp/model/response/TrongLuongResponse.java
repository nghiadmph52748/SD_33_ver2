package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.TrongLuong;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrongLuongResponse {
    Integer id;
    String maTrongLuong;
    String tenTrongLuong;
    Boolean deleted;
    Boolean trangThai;
    LocalDate createAt;
    LocalDate updateAt;
    Integer createBy;
    Integer updateBy;

    public TrongLuongResponse(TrongLuong d) {
        this.id = d.getId();
        this.maTrongLuong = d.getMaTrongLuong();
        this.tenTrongLuong = d.getTenTrongLuong();
        this.deleted = d.getDeleted();
        this.trangThai = d.getTrangThai();
        this.createAt = d.getCreateAt();
        this.createBy = d.getCreateBy();
        this.updateAt = d.getUpdateAt();
        this.updateBy = d.getUpdateBy();
    }
}
