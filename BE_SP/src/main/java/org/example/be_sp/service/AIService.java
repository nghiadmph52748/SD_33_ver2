package org.example.be_sp.service;

import org.example.be_sp.model.ai.ChatRequest;
import org.example.be_sp.model.ai.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIService {

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;

    public AIService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send chat message to AI service
     */
    public ChatResponse chat(String message) {
        String url = aiServiceUrl + "/api/ai/chatbot/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("message", message);
        requestBody.put("context", "");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<ChatResponse> response = restTemplate.postForEntity(
                url,
                entity,
                ChatResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to AI service: " + e.getMessage());
        }
    }

    /**
     * Check AI service health
     */
    public Map<String, Object> checkHealth() {
        String url = aiServiceUrl + "/api/ai/chatbot/health";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("AI service is unavailable: " + e.getMessage());
        }
    }
}
