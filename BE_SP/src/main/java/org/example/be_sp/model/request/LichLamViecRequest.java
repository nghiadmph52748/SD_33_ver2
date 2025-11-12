package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class LichLamViecRequest {
    private Integer nhanVienId;
    private Long caLamViecId;
    private LocalDate ngayLamViec;
    private Boolean trangThai;
    private String ghiChu;
}
