package org.example.be_sp.model.response;

import java.time.LocalDateTime;

import org.example.be_sp.entity.TrangThaiDonHang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrangThaiDonHangResponse {

    private Integer id;
    private Integer idHoaDon;
    private Integer idTrangThaiDonHang;
    private String maTrangThaiDonHang;
    private LocalDateTime thoiGian;
}
