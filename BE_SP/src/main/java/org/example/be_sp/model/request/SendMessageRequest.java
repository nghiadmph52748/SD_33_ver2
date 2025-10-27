package org.example.be_sp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO để gửi tin nhắn mới
 */
@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {
    
    @NotNull(message = "ID người nhận không được để trống")
    private Integer receiverId;
    
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
    
    private String messageType = "TEXT"; // TEXT, IMAGE, FILE
    
    // Chỉ dùng khi messageType là IMAGE hoặc FILE
    private String fileUrl;
}
