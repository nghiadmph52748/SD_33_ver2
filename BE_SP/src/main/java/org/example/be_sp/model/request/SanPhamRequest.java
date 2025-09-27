package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SanPhamRequest {
    private String tenSanPham;
    private Integer idNhaSanXuat;
    private Integer idXuatXu;
    private Boolean deleted;
    private Boolean trangThai;
    private LocalDate createAt;
    private LocalDate updateAt;
    private Integer createBy;
    private Integer updateBy;
}
