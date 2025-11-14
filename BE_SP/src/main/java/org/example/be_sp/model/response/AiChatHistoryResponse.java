package org.example.be_sp.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.be_sp.entity.AiChatHistory;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiChatHistoryResponse {
    private Integer id;
    private Integer customerId;
    private String sessionId;
    private String role;
    private String content;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;
    private Integer createdBy;

    public AiChatHistoryResponse(AiChatHistory entity) {
        this.id = entity.getId();
        this.customerId = entity.getKhachHang() != null ? entity.getKhachHang().getId() : null;
        this.sessionId = entity.getSessionId();
        this.role = entity.getRole();
        this.content = entity.getContent();
        this.timestamp = entity.getTimestamp();
        this.createdAt = entity.getCreatedAt();
        this.createdBy = entity.getCreatedBy();
    }
}

