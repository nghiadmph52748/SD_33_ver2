package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class NhaSanXuatRequest {
    private String tenNhaSanXuat;
    private Boolean trangThai;
    private Boolean deleted;
    LocalDate createAt;
    Integer createBy;
    LocalDate updateAt;
    Integer updateBy;
}
