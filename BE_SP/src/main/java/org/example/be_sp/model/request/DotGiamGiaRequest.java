package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaRequest {
    String tenDotGiamGia;
    Integer giaTriGiamGia;
    LocalDate ngayBatDau;
    LocalDate ngayKetThuc;
    Boolean trangThai;
    Boolean deleted;
}
