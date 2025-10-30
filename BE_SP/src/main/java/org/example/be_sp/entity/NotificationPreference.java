package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User notification preferences (Cài đặt thông báo người dùng)
 * Controls which types of notifications the user wants to receive
 */
@Entity
@Table(name = "cai_dat_thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung", unique = true)
    private NhanVien user;
    
    // Notification type preferences
    @Column(name = "bat_thong_bao_don_hang")
    private Boolean enableOrderNotifications = true;
    
    @Column(name = "bat_canh_bao_ton_kho")
    private Boolean enableStockAlerts = true;
    
    @Column(name = "bat_thong_bao_he_thong")
    private Boolean enableSystemNotices = true;
    
    @Column(name = "bat_tin_nhan_chat")
    private Boolean enableChatMessages = true;
    
    // Delivery preferences
    @Column(name = "bat_thong_bao_web")
    private Boolean enableWebPush = true;
    
    @Column(name = "bat_thong_bao_email")
    private Boolean enableEmail = false;
    
    // Quiet hours (Giờ im lặng)
    @Column(name = "bat_che_do_im_lang")
    private Boolean enableQuietHours = false;
    
    @Column(name = "gio_bat_dau_im_lang")
    private Integer quietStartHour = 22;  // 10 PM
    
    @Column(name = "gio_ket_thuc_im_lang")
    private Integer quietEndHour = 7;     // 7 AM
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @Column(name = "create_at")
    private java.time.LocalDate createAt;
    
    @Column(name = "create_by")
    private Integer createBy;
    
    @Column(name = "update_at")
    private java.time.LocalDate updateAt;
    
    @Column(name = "update_by")
    private Integer updateBy;
}
