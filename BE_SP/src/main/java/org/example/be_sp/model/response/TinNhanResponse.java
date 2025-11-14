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
    private String loaiTinNhanType; // STAFF_STAFF, CUSTOMER_STAFF
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
        this.loaiTinNhanType = tinNhan.getLoaiTinNhanType();
        this.content = tinNhan.getNoiDung();
        this.messageType = tinNhan.getLoaiTinNhan();
        this.isRead = tinNhan.getDaDoc();
        this.sentAt = tinNhan.getThoiGianGui();
        this.readAt = tinNhan.getThoiGianDoc();
        
        // Set sender and receiver based on message type
        if (tinNhan.getLoaiTinNhanType() != null && tinNhan.getLoaiTinNhanType().equals("CUSTOMER_STAFF")) {
            // Customer-staff message
            if (tinNhan.getKhachHangGui() != null) {
                // Customer is sender, staff is receiver
                this.senderId = tinNhan.getKhachHangGui().getId();
                this.senderName = tinNhan.getKhachHangGui().getTenKhachHang();
                if (tinNhan.getNguoiNhan() != null) {
                    this.receiverId = tinNhan.getNguoiNhan().getId();
                    this.receiverName = tinNhan.getNguoiNhan().getTenNhanVien();
                }
            } else if (tinNhan.getNguoiGui() != null) {
                // Staff is sender, customer is receiver
                this.senderId = tinNhan.getNguoiGui().getId();
                this.senderName = tinNhan.getNguoiGui().getTenNhanVien();
                if (tinNhan.getKhachHangNhan() != null) {
                    this.receiverId = tinNhan.getKhachHangNhan().getId();
                    this.receiverName = tinNhan.getKhachHangNhan().getTenKhachHang();
                }
            }
        } else {
            // Staff-staff message
            if (tinNhan.getNguoiGui() != null) {
                this.senderId = tinNhan.getNguoiGui().getId();
                this.senderName = tinNhan.getNguoiGui().getTenNhanVien();
            }
            if (tinNhan.getNguoiNhan() != null) {
                this.receiverId = tinNhan.getNguoiNhan().getId();
                this.receiverName = tinNhan.getNguoiNhan().getTenNhanVien();
            }
        }
    }
}
