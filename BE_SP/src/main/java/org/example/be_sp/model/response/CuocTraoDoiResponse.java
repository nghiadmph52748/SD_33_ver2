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
    private Integer nhanVien1Id;
    private String nhanVien1Name;
    private Integer nhanVien2Id;
    private String nhanVien2Name;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
    private Integer lastSenderId;
    private Integer unreadCountNv1;
    private Integer unreadCountNv2;
    
    public CuocTraoDoiResponse(CuocTraoDoi cuocTraoDoi) {
        this.id = cuocTraoDoi.getId();
        this.maCuocTraoDoi = cuocTraoDoi.getMaCuocTraoDoi();
        this.nhanVien1Id = cuocTraoDoi.getNhanVien1().getId();
        this.nhanVien1Name = cuocTraoDoi.getNhanVien1().getTenNhanVien();
        this.nhanVien2Id = cuocTraoDoi.getNhanVien2().getId();
        this.nhanVien2Name = cuocTraoDoi.getNhanVien2().getTenNhanVien();
        this.lastMessageContent = cuocTraoDoi.getTinNhanCuoiCung();
        this.lastMessageTime = cuocTraoDoi.getThoiGianTinNhanCuoi();
        this.lastSenderId = cuocTraoDoi.getNguoiGuiCuoi() != null ? cuocTraoDoi.getNguoiGuiCuoi().getId() : null;
        this.unreadCountNv1 = cuocTraoDoi.getSoTinNhanChuaDocNv1();
        this.unreadCountNv2 = cuocTraoDoi.getSoTinNhanChuaDocNv2();
    }
}
