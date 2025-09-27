package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrangThaiDonHangRequest {
    private String tenTrangThaiDonHang;
    private Boolean deleted;
}
