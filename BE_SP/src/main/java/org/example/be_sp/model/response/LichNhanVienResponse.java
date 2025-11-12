package org.example.be_sp.model.response;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class LichNhanVienResponse {
    private Long id;
    private String tenNhanVien;
    private String tenCa;
    private LocalDate ngayLamViec;
}
