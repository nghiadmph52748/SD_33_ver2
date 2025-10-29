package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ThongTinHoaDonRequest {
    private Integer idHoaDon;
    private Integer idTrangThaiDonHang;
    private String ghiChu;
}
