package org.example.be_sp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_ai_chat")
@Data
public class AiChatHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;
    
    @Column(name = "session_id", nullable = false, length = 255)
    private String sessionId;
    
    @Column(name = "role", nullable = false, length = 50)
    private String role; // 'user' or 'assistant'
    
    @Column(name = "content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Integer createdBy;
}

