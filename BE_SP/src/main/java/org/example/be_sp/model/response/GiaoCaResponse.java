package org.example.be_sp.model.response;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GiaoCaResponse {
    private Integer id;               // ID giao ca
    private Integer nguoiGiaoId;      // ID người giao
    private String tenNguoiGiao;      // Tên người giao
    private Integer nguoiNhanId;      // ID người nhận
    private String tenNguoiNhan;      // Tên người nhận
    private Integer caLamViecId;      // ID ca làm việc
    private String tenCa;             // Tên ca
    private LocalDateTime thoiGianGiaoCa; // Thời gian giao ca
    private Double tongTienBanDau;    // Tổng tiền đầu ca
    private Double tongTienCuoiCa;    // Tổng tiền cuối ca (có thể null khi giao ca)
    private String ghiChu;            // Ghi chú (nếu có)
}
