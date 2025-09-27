package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PhieuGiamGiaCaNhanRequest {
    Integer idKhachHang;
    Integer idPhieuGiamGia;
    String tenPhieuGiamGiaCaNhan;
    LocalDate ngayNhan;
    LocalDate ngayHetHan;
    Boolean trangThai;
    Boolean deleted;
}
