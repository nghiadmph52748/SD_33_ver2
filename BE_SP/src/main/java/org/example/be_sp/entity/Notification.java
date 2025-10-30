package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "thong_bao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "loai", nullable = false, length = 20)
    private String type; // message, notice, todo
    
    @Column(name = "tieu_de", nullable = false, length = 200)
    private String title;
    
    @Column(name = "tieu_de_phu", length = 200)
    private String subTitle;
    
    @Column(name = "anh_dai_dien", length = 500)
    private String avatar;
    
    @Column(name = "noi_dung", columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(name = "thoi_gian_tao", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "trang_thai", nullable = false)
    private Integer status; // 0 = chưa đọc, 1 = đã đọc
    
    @Column(name = "loai_tin_nhan")
    private Integer messageType; // 0 = chưa bắt đầu, 1 = đã mở, 2 = đang xử lý, 3 = sắp hết hạn
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nguoi_dung")
    private NhanVien user; // User who receives this notification
    
    @Column(name = "deleted")
    private Boolean deleted = false;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = 0; // Default to unread
        }
        if (deleted == null) {
            deleted = false;
        }
    }
}
