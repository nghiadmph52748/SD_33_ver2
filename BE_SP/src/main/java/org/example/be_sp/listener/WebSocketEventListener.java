package org.example.be_sp.listener;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.repository.NhanVienRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listener để track khi user connect/disconnect WebSocket
 * Quản lý online status của users
 */
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    // Track online users: sessionId → userId
    private final Map<String, Integer> sessionUserMap = new ConcurrentHashMap<>();
    
    // Track user sessions: userId → Set<sessionId> (user có thể mở nhiều tabs)
    private final Map<Integer, Set<String>> userSessionsMap = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final NhanVienRepository nhanVienRepository;

    public WebSocketEventListener(
            SimpMessagingTemplate messagingTemplate,
            NhanVienRepository nhanVienRepository) {
        this.messagingTemplate = messagingTemplate;
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Xử lý khi user connect WebSocket
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // Lấy username từ Principal
        String username = headerAccessor.getUser() != null 
            ? headerAccessor.getUser().getName() 
            : null;

        if (username != null && sessionId != null) {
            try {
                // Lấy userId từ username
                NhanVien nhanVien = nhanVienRepository.findByTenTaiKhoan(username)
                    .orElse(null);
                
                if (nhanVien != null) {
                    Integer userId = nhanVien.getId();
                    
                    // Track session → user mapping
                    sessionUserMap.put(sessionId, userId);
                    
                    // Track user → sessions mapping
                    userSessionsMap.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                        .add(sessionId);
                    
                    logger.info("User {} (ID: {}) connected. Session: {}", 
                        username, userId, sessionId);
                    
                    // Broadcast presence update
                    broadcastPresenceUpdate(userId, "ONLINE");
                }
            } catch (Exception e) {
                logger.error("Error handling connect event: {}", e.getMessage());
            }
        }
    }

    /**
     * Xử lý khi user disconnect WebSocket
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            // Lấy userId từ session
            Integer userId = sessionUserMap.remove(sessionId);
            
            if (userId != null) {
                // Remove session from user's session set
                Set<String> userSessions = userSessionsMap.get(userId);
                if (userSessions != null) {
                    userSessions.remove(sessionId);
                    
                    // Nếu user không còn session nào (đã đóng hết tabs)
                    if (userSessions.isEmpty()) {
                        userSessionsMap.remove(userId);
                        
                        logger.info("User {} disconnected completely. Session: {}", 
                            userId, sessionId);
                        
                        // Broadcast offline status
                        broadcastPresenceUpdate(userId, "OFFLINE");
                    } else {
                        logger.info("📱 User {} still has {} active session(s)", 
                            userId, userSessions.size());
                    }
                }
            }
        }
    }

    /**
     * Broadcast presence update tới tất cả users
     */
    private void broadcastPresenceUpdate(Integer userId, String status) {
        try {
            PresenceUpdate update = new PresenceUpdate(
                userId, 
                status, 
                System.currentTimeMillis()
            );
            
            // Gửi tới topic để tất cả clients nhận được
            messagingTemplate.convertAndSend(
                "/topic/presence", 
                update
            );
            
            logger.info("Broadcast presence: User {} is {}", userId, status);
        } catch (Exception e) {
            logger.error("Error broadcasting presence: {}", e.getMessage());
        }
    }

    /* Check xem user có online không */
    public boolean isUserOnline(Integer userId) {
        Set<String> sessions = userSessionsMap.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /**
     * Lấy tất cả online users
     */
    public Set<Integer> getOnlineUsers() {
        return userSessionsMap.keySet();
    }

    /**
     * DTO cho presence update
     */
    public static class PresenceUpdate {
        private Integer userId;
        private String status; // ONLINE, OFFLINE, AWAY
        private Long timestamp;

        public PresenceUpdate(Integer userId, String status, Long timestamp) {
            this.userId = userId;
            this.status = status;
            this.timestamp = timestamp;
        }

        // Getters and setters
        public Integer getUserId() { return userId; }
        public void setUserId(Integer userId) { this.userId = userId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getTimestamp() { return timestamp; }
        public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
    }
}
