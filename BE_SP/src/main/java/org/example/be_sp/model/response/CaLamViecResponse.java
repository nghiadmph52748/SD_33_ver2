package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class CaLamViecResponse {
    private Long id;
    private String tenCa;
    private LocalTime gioBatDau;
    private LocalTime gioKetThuc;
    private Boolean trangThai;
}
