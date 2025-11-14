package org.example.be_sp.model.response;

import java.time.LocalDateTime;

import org.example.be_sp.entity.CuocTraoDoi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO cho cuộc trò chuyện
 */
@Getter
@Setter
@NoArgsConstructor
public class CuocTraoDoiResponse {
    
    private Integer id;
    private String maCuocTraoDoi;
    private String loaiCuocTraoDoi; // STAFF_STAFF, CUSTOMER_STAFF
    // Staff-staff fields
    private Integer nhanVien1Id;
    private String nhanVien1Name;
    private Integer nhanVien2Id;
    private String nhanVien2Name;
    // Customer-staff fields
    private Integer khachHangId;
    private String khachHangName;
    private Integer nhanVienId;
    private String nhanVienName;
    // Common fields
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private Integer lastSenderId;
    private Integer unreadCountNv1;
    private Integer unreadCountNv2;
    
    public CuocTraoDoiResponse(CuocTraoDoi cuocTraoDoi) {
        this.id = cuocTraoDoi.getId();
        this.maCuocTraoDoi = cuocTraoDoi.getMaCuocTraoDoi();
        this.loaiCuocTraoDoi = cuocTraoDoi.getLoaiCuocTraoDoi();
        this.lastMessageContent = cuocTraoDoi.getTinNhanCuoiCung();
        this.lastMessageTime = cuocTraoDoi.getThoiGianTinNhanCuoi();
        this.unreadCountNv1 = cuocTraoDoi.getSoTinNhanChuaDocNv1();
        this.unreadCountNv2 = cuocTraoDoi.getSoTinNhanChuaDocNv2();
        
        // Set last sender ID
        if (cuocTraoDoi.getNguoiGuiCuoi() != null) {
            this.lastSenderId = cuocTraoDoi.getNguoiGuiCuoi().getId();
        } else if (cuocTraoDoi.getKhachHangGuiCuoi() != null) {
            this.lastSenderId = cuocTraoDoi.getKhachHangGuiCuoi().getId();
        }
        
        // Set fields based on conversation type
        if (cuocTraoDoi.getLoaiCuocTraoDoi() != null && cuocTraoDoi.getLoaiCuocTraoDoi().equals("CUSTOMER_STAFF")) {
            // Customer-staff conversation
            if (cuocTraoDoi.getKhachHang() != null) {
                this.khachHangId = cuocTraoDoi.getKhachHang().getId();
                this.khachHangName = cuocTraoDoi.getKhachHang().getTenKhachHang();
            }
            if (cuocTraoDoi.getNhanVien() != null) {
                this.nhanVienId = cuocTraoDoi.getNhanVien().getId();
                this.nhanVienName = cuocTraoDoi.getNhanVien().getTenNhanVien();
            }
        } else {
            // Staff-staff conversation
            if (cuocTraoDoi.getNhanVien1() != null) {
                this.nhanVien1Id = cuocTraoDoi.getNhanVien1().getId();
                this.nhanVien1Name = cuocTraoDoi.getNhanVien1().getTenNhanVien();
            }
            if (cuocTraoDoi.getNhanVien2() != null) {
                this.nhanVien2Id = cuocTraoDoi.getNhanVien2().getId();
                this.nhanVien2Name = cuocTraoDoi.getNhanVien2().getTenNhanVien();
            }
        }
    }
}
