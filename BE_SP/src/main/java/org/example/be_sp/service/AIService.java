package org.example.be_sp.service;

import org.example.be_sp.model.ai.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AIService {

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    private final RestTemplate restTemplate;
    private final ExecutorService executorService;

    public AIService() {
        this.restTemplate = new RestTemplate();
        this.executorService = Executors.newCachedThreadPool();
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
    @SuppressWarnings("unchecked")
    public Map<String, Object> checkHealth() {
        String url = aiServiceUrl + "/api/ai/chatbot/health";

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("AI service is unavailable: " + e.getMessage());
        }
    }

    /**
     * Send chat message to AI service with streaming response
     */
    public SseEmitter chatStream(String message) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout
        String url = aiServiceUrl + "/api/ai/chatbot/chat-stream";

        executorService.execute(() -> {
            BufferedReader reader = null;
            try {
                // Create connection
                URL streamUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) streamUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Accept", "text/event-stream");
                connection.setDoOutput(true);
                connection.setConnectTimeout(10000); // 10 seconds
                connection.setReadTimeout(300000); // 5 minutes

                // Send request body
                String jsonBody = String.format("{\"message\":\"%s\",\"context\":\"\"}", 
                    message.replace("\"", "\\\"").replace("\n", "\\n"));
                connection.getOutputStream().write(jsonBody.getBytes("UTF-8"));
                connection.getOutputStream().flush();

                // Check response code
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new RuntimeException("HTTP error: " + responseCode);
                }

                // Read streaming response
                reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8")
                );

                String line;
                
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        // Extract JSON data
                        String jsonData = line.substring(6).trim();
                        if (!jsonData.isEmpty()) {
                            // Send as SSE event with proper format
                            emitter.send(SseEmitter.event()
                                .data(jsonData)
                                .name("message"));
                        }
                    }
                }

                emitter.complete();

            } catch (Exception e) {
                e.printStackTrace();
                emitter.completeWithError(e);
            } finally {
                try {
                    if (reader != null) reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return emitter;
    }
}
