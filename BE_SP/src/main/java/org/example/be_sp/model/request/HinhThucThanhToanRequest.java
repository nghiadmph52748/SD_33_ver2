package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class HinhThucThanhToanRequest {
    private Integer idHoaDon;
    private Integer idPhuongThucThanhToan;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienMat;
    private Boolean trangThai;
    private Boolean deleted;
}
