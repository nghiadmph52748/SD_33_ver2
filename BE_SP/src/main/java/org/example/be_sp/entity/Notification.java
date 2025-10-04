package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "type", nullable = false, length = 20)
    private String type; // message, notice, todo
    
    @Column(name = "title", nullable = false, length = 200)
    private String title;
    
    @Column(name = "sub_title", length = 200)
    private String subTitle;
    
    @Column(name = "avatar", length = 500)
    private String avatar;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "status", nullable = false)
    private Integer status; // 0 = unread, 1 = read
    
    @Column(name = "message_type")
    private Integer messageType; // 0 = not started, 1 = opened, 2 = in progress, 3 = almost expired
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
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
