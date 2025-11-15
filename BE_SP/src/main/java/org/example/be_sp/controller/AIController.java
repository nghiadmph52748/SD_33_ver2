package org.example.be_sp.controller;

import org.example.be_sp.model.ai.ChatRequest;
import org.example.be_sp.model.ai.ChatResponse;
import org.example.be_sp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    // ========== Customer Endpoints ==========
    
    /**
     * Chat with customer support AI assistant
     * POST /api/ai/chatbot/customer/chat
     */
    @PostMapping("/chatbot/customer/chat")
    public ResponseEntity<ChatResponse> chatCustomer(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = aiService.chatCustomer(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ChatResponse.builder()
                    .message("❌ Lỗi: " + e.getMessage())
                    .sources("")
                    .queryType("error")
                    .build());
        }
    }

    /**
     * Chat with customer support AI assistant - Streaming response
     * POST /api/ai/chatbot/customer/chat-stream
     */
    @PostMapping(value = "/chatbot/customer/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamCustomer(@RequestBody ChatRequest request) {
        return aiService.chatStreamCustomer(request.getMessage());
    }

    /**
     * Check customer AI service health
     * GET /api/ai/chatbot/customer/health
     */
    @GetMapping("/chatbot/customer/health")
    public ResponseEntity<?> healthCustomer() {
        try {
            return ResponseEntity.ok(aiService.checkHealthCustomer());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("AI Service unavailable: " + e.getMessage());
        }
    }

    // ========== Admin Endpoints ==========

    /**
     * Chat with admin AI assistant
     * POST /api/ai/chatbot/admin/chat
     */
    @PostMapping("/chatbot/admin/chat")
    public ResponseEntity<ChatResponse> chatAdmin(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = aiService.chatAdmin(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ChatResponse.builder()
                    .message("❌ Lỗi: " + e.getMessage())
                    .sources("")
                    .queryType("error")
                    .build());
        }
    }

    /**
     * Chat with admin AI assistant - Streaming response
     * POST /api/ai/chatbot/admin/chat-stream
     */
    @PostMapping(value = "/chatbot/admin/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStreamAdmin(@RequestBody ChatRequest request) {
        return aiService.chatStreamAdmin(request.getMessage());
    }

    /**
     * Check admin AI service health
     * GET /api/ai/chatbot/admin/health
     */
    @GetMapping("/chatbot/admin/health")
    public ResponseEntity<?> healthAdmin() {
        try {
            return ResponseEntity.ok(aiService.checkHealthAdmin());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("AI Service unavailable: " + e.getMessage());
        }
    }

    // ========== Legacy Endpoints (for backward compatibility) ==========

    /**
     * Chat with AI assistant (legacy - defaults to admin)
     * POST /api/ai/chat
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            ChatResponse response = aiService.chat(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(ChatResponse.builder()
                    .message("❌ Lỗi: " + e.getMessage())
                    .sources("")
                    .queryType("error")
                    .build());
        }
    }

    /**
     * Chat with AI assistant - Streaming response (legacy - defaults to admin)
     * POST /api/ai/chat-stream
     */
    @PostMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody ChatRequest request) {
        return aiService.chatStream(request.getMessage());
    }

    /**
     * Check AI service health (legacy - defaults to admin)
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            return ResponseEntity.ok(aiService.checkHealth());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body("AI Service unavailable: " + e.getMessage());
        }
    }

    /**
     * Get AI suggestions for POS
     * POST /api/ai/suggestions
     */
    @PostMapping("/suggestions")
    public ResponseEntity<?> getSuggestions(@RequestBody Long[] productIds) {
        // TODO: Implement product suggestions (Apriori algorithm)
        return ResponseEntity.ok("Feature coming soon");
    }
}
