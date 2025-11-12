package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class GiaoCaRequest {
    private Integer nguoiGiaoId;
    private Integer nguoiNhanId;
    private Long caLamViecId;
    private LocalDateTime thoiGianGiaoCa;

    private Double tongTienBanDau;
    private Double tongTienCuoiCa;
    private Double tongTienMat;
    private Double tongTienChuyenKhoan;
    private Double tongTienKhac;
    private Double tongDoanhThu;
    private Double tienPhatSinh;
    private Double tongTienThucTe;
    private Double chenhlech;
    private Double tienMatNopLai;

    private String ghiChu;
}
