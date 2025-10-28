package org.example.be_sp.controller;

import org.example.be_sp.model.ai.ChatRequest;
import org.example.be_sp.model.ai.ChatResponse;
import org.example.be_sp.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * Chat with AI assistant
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
     * Check AI service health
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
