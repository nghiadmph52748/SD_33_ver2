package org.example.be_sp.service;

import lombok.extern.slf4j.Slf4j;
import org.example.be_sp.controller.NotificationWebSocketController;
import org.example.be_sp.entity.Notification;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.repository.NotificationRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NhanVienRepository nhanVienRepository;
    private final NotificationWebSocketController webSocketController;
    
    public NotificationService(
            NotificationRepository notificationRepository,
            NhanVienRepository nhanVienRepository,
            @Lazy NotificationWebSocketController webSocketController) {
        this.notificationRepository = notificationRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.webSocketController = webSocketController;
    }
    
    /**
     * Get all notifications for a user
     */
    public List<NotificationResponse> getNotificationsForUser(Integer userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedOrderByCreatedAtDesc(userId, false);
        return notifications.stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get unread notifications count for a user
     */
    public Long getUnreadCount(Integer userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }
    
    /**
     * Mark notifications as read
     */
    @Transactional
    public void markAsRead(List<Integer> ids) {
        for (Integer id : ids) {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new ApiException("Notification not found", "404"));
            notification.setStatus(1);
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(Integer userId, String type, String title, 
                                          String subTitle, String content, Integer messageType) {
        NhanVien user = nhanVienRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", "404"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setSubTitle(subTitle);
        notification.setContent(content);
        notification.setMessageType(messageType);
        notification.setStatus(0); // Unread
        notification.setDeleted(false);
        notification.setCreatedAt(LocalDateTime.now());
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Push notification via WebSocket
        try {
            NotificationResponse response = NotificationResponse.fromEntity(savedNotification);
            webSocketController.sendNotificationToUser(userId, response);
            log.info("📬 Real-time notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send real-time notification: {}", e.getMessage());
        }
        
        return savedNotification;
    }
    
    /**
     * Create notification for all users (broadcast)
     */
    @Transactional
    public void createBroadcastNotification(String type, String title, String subTitle, 
                                           String content, Integer messageType) {
        List<NhanVien> allUsers = nhanVienRepository.findAll();
        
        for (NhanVien user : allUsers) {
            createNotification(user.getId(), type, title, subTitle, content, messageType);
        }
        
        log.info("📢 Broadcast notification created for {} users", allUsers.size());
    }
    
    /**
     * Delete notification (soft delete)
     */
    @Transactional
    public void deleteNotification(Integer id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Notification not found", "404"));
        notification.setDeleted(true);
        notificationRepository.save(notification);
    }
    
    /**
     * Clear all notifications for a user
     */
    @Transactional
    public void clearAllForUser(Integer userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndDeletedOrderByCreatedAtDesc(userId, false);
        
        for (Notification notification : notifications) {
            notification.setDeleted(true);
            notificationRepository.save(notification);
        }
    }
}
