package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DiaChiKhachHangRequest {
    Integer idKhachHang;
    String tenDiaChi;
    String thanhPho;
    String quan;
    String phuong;
    String diaChiCuThe;
    Boolean deleted;
}
