package org.example.be_sp.model.response;

import java.time.LocalDateTime;

import org.example.be_sp.entity.TinNhan;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response DTO cho tin nhắn
 */
@Getter
@Setter
@NoArgsConstructor
public class TinNhanResponse {
    
    private Integer id;
    private String maTinNhan;
    private Integer senderId;
    private String senderName;
    private Integer receiverId;
    private String receiverName;
    private String content;
    private String messageType;
    private Boolean isRead;
    private LocalDateTime sentAt;
    private LocalDateTime readAt;
    
    public TinNhanResponse(TinNhan tinNhan) {
        this.id = tinNhan.getId();
        this.maTinNhan = tinNhan.getMaTinNhan();
        this.senderId = tinNhan.getNguoiGui().getId();
        this.senderName = tinNhan.getNguoiGui().getTenNhanVien();
        this.receiverId = tinNhan.getNguoiNhan().getId();
        this.receiverName = tinNhan.getNguoiNhan().getTenNhanVien();
        this.content = tinNhan.getNoiDung();
        this.messageType = tinNhan.getLoaiTinNhan();
        this.isRead = tinNhan.getDaDoc();
        this.sentAt = tinNhan.getThoiGianGui();
        this.readAt = tinNhan.getThoiGianDoc();
    }
}
