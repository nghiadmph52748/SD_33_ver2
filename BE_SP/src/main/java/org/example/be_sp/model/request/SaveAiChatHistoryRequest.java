package org.example.be_sp.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveAiChatHistoryRequest {
    @NotNull(message = "Customer ID is required")
    private Integer customerId;
    
    @NotBlank(message = "Session ID is required")
    private String sessionId;
    
    @NotBlank(message = "Role is required")
    private String role; // 'user' or 'assistant'
    
    @NotBlank(message = "Content is required")
    private String content;
}

