package org.example.be_sp.model.ai;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String context;
}
