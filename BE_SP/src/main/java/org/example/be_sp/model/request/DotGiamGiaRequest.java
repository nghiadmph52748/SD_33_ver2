package org.example.be_sp.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaRequest {
    String tenDotGiamGia;
    Integer giaTriGiamGia;
    LocalDateTime ngayBatDau;
    LocalDateTime ngayKetThuc;
    Boolean trangThai;
    Boolean deleted;
    String lyDoThayDoi;
}
