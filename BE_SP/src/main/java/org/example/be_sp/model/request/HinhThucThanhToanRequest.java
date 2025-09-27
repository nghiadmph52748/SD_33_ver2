package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class HinhThucThanhToanRequest {
    private Integer idHoaDon;
    private Integer idPhuongThucThanhToan;
    private BigDecimal tienChuyenKhoan;
    private BigDecimal tienMat;
    private Boolean deleted;
}
