package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.be_sp.entity.DeGiay;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DeGiayResponse {
    private Integer id;
    private String maDeGiay;
    private String tenDeGiay;
    private Boolean deleted;
    private Boolean trangThai;
    private Integer createBy;
    private LocalDate createAt;
    private Integer updateBy;
    private LocalDate updateAt;

    public DeGiayResponse(DeGiay data) {
        this.id = data.getId();
        this.maDeGiay = data.getMaDeGiay();
        this.tenDeGiay = data.getTenDeGiay();
        this.deleted = data.getDeleted();
        this.trangThai = data.getTrangThai();
        this.createAt = data.getCreateAt();
        this.createBy = data.getCreateBy();
        this.updateAt = data.getUpdateAt();
        this.updateBy = data.getUpdateBy();
    }
}
