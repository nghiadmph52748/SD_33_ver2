package org.example.be_sp.model.response;

import java.time.Instant;

import org.example.be_sp.entity.TimelineDonHang;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimelineDonHangResponse {
    private Integer id;
    private Integer idHoaDon;
    private Integer idNhanVien;
    private String tenNhanVien;
    private String maTimeline;
    private String trangThaiCu;
    private String trangThaiMoi;
    private String hanhDong;
    private String moTa;
    private String ghiChu;
    private Instant thoiGian;
    private String ipAddress;
    private String userAgent;
    private Boolean trangThai;
    private Boolean deleted;

    public TimelineDonHangResponse(TimelineDonHang data) {
        if (data == null) {
            return;
        }
        
        this.id = data.getId();
        this.maTimeline = data.getMaTimeline();
        this.trangThaiCu = data.getTrangThaiCu();
        this.trangThaiMoi = data.getTrangThaiMoi();
        this.hanhDong = data.getHanhDong();
        this.moTa = data.getMoTa();
        this.ghiChu = data.getGhiChu();
        this.thoiGian = data.getThoiGian();
        this.trangThai = data.getTrangThai();
        this.deleted = data.getDeleted();
        
        if (data.getIdHoaDon() != null) {
            this.idHoaDon = data.getIdHoaDon().getId();
        }
        
        if (data.getIdNhanVien() != null) {
            this.idNhanVien = data.getIdNhanVien().getId();
            this.tenNhanVien = data.getIdNhanVien().getTenNhanVien();
        }
    }
}

