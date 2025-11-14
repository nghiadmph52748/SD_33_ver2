package org.example.be_sp.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GiaoCaRequest {

    // Thông tin người giao và người nhận
    private Integer nguoiGiaoId;
    private Integer nguoiNhanId;
    private Long caLamViecId;

    // Thời gian bàn giao
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianGiaoCa;

    // Tiền bàn giao
    private Double tongTienBanDau;
    private Double tongTienCuoiCa;

    // Các loại tiền trong ca
    private Double tongTienMat;
    private Double tongTienChuyenKhoan;
    private Double tongTienKhac;
    private Double tongDoanhThu;

    // Phát sinh và thực tế
    private Double tienPhatSinh;
    private Double tongTienThucTe;
    private Double chenhlech;
    private Double tienMatNopLai;

    // Ghi chú bàn giao
    private String ghiChu;

    // ====== CÁC TRƯỜNG XÁC NHẬN CA ======
    private Double soTienNhanThucTe; // số tiền thực tế người nhận nhập
    private String trangThaiXacNhan; // 'Chưa xác nhận', 'Đã xác nhận', 'Chênh lệch', 'Từ chối'

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime thoiGianXacNhan; // thời gian xác nhận
    private String ghiChuXacNhan; // ghi chú lý do nếu có chênh lệch
    private String trangThaiCa; // 'Đang làm', 'Chờ xác nhận', 'Hoàn tất'
}
