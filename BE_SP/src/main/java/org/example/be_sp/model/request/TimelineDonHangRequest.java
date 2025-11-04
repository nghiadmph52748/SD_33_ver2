package org.example.be_sp.model.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimelineDonHangRequest {
    private Integer idHoaDon;
    private Integer idNhanVien;
    private String tenNhanVien;
    private String trangThaiCu;
    private String trangThaiMoi;
    private String hanhDong;
    private String moTa;
    private String ghiChu;
    private String thoiGian; // Format: "YYYY-MM-DD HH:mm:ss"
    private String ipAddress;
    private String userAgent;
}

