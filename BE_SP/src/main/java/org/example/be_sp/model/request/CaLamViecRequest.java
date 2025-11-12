package org.example.be_sp.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class CaLamViecRequest {
    private String tenCa;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gioBatDau;

    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime gioKetThuc;

    private Boolean trangThai;
}
