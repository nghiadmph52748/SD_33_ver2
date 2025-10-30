package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.service.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket Controller for real-time notifications
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketController {
    
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NhanVienRepository nhanVienRepository;
    
    /**
     * Send notification to a specific user
     * This method can be called from anywhere in the backend to push notifications
     */
    public void sendNotificationToUser(Integer userId, NotificationResponse notification) {
        try {
            // Get username from userId
            NhanVien user = nhanVienRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            
            String username = user.getTenTaiKhoan();
            
            // Send notification via WebSocket
            messagingTemplate.convertAndSendToUser(
                username,
                "/queue/notifications",
                notification
            );
            
            log.info("📬 Sent notification to user {}: {}", username, notification.getTitle());
        } catch (Exception e) {
            log.error("❌ Error sending notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    /**
     * Broadcast notification to all connected users
     * This can be used for system-wide announcements
     */
    public void broadcastNotification(NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSend("/topic/notifications", notification);
            log.info("📢 Broadcast notification: {}", notification.getTitle());
        } catch (Exception e) {
            log.error("❌ Error broadcasting notification: {}", e.getMessage());
        }
    }
    
    /**
     * Handle notification read acknowledgment from client
     * Client sends to: /app/notification.read
     */
    @MessageMapping("/notification.read")
    public void markNotificationAsRead(@Payload Integer notificationId, Principal principal) {
        try {
            log.info("Marking notification {} as read by {}", notificationId, principal.getName());
            notificationService.markAsRead(java.util.List.of(notificationId));
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", e.getMessage());
        }
    }
}
