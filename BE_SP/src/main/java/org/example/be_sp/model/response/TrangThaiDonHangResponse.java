package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrangThaiDonHangResponse {
    private Integer id;
    private String maTrangThaiDonHang;
    private String tenTrangThaiDonHang;
    private Boolean deleted;
}
