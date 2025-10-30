package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.annotation.RequireAuth;
import org.example.be_sp.entity.Notification;
import org.example.be_sp.entity.NotificationPreference;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.NotificationPreferenceService;
import org.example.be_sp.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "*")
@RequireAuth
@RequiredArgsConstructor
public class NotificationController {
    
    private final NotificationService notificationService;
    private final NotificationPreferenceService preferenceService;
    
    // TODO: Get actual user ID from SecurityContext
    // For now, use ID 1 for testing
    private Integer getCurrentUserId() {
        // This should be replaced with actual SecurityUtils.getCurrentUserId()
        // when that method is implemented
        return 1;
    }
    
    /**
     * Get all notifications for current user
     * POST /api/message/list
     */
    @PostMapping("/list")
    public ResponseObject<List<NotificationResponse>> getNotifications() {
        Integer userId = getCurrentUserId();
        List<NotificationResponse> notifications = notificationService.getNotificationsForUser(userId);
        return new ResponseObject<>(notifications, "Lấy danh sách thông báo thành công");
    }
    
    /**
     * Mark notifications as read
     * POST /api/message/read
     * Body: { "ids": [1, 2, 3] }
     */
    @PostMapping("/read")
    public ResponseObject<Boolean> markAsRead(@RequestBody Map<String, List<Integer>> request) {
        List<Integer> ids = request.get("ids");
        if (ids == null || ids.isEmpty()) {
            return ResponseObject.error("Danh sách ID không được rỗng");
        }
        notificationService.markAsRead(ids);
        return new ResponseObject<>(true, "Đánh dấu đã đọc thành công");
    }
    
    /**
     * Get unread count for current user
     * GET /api/message/unread-count
     */
    @GetMapping("/unread-count")
    public ResponseObject<Long> getUnreadCount() {
        Integer userId = getCurrentUserId();
        Long count = notificationService.getUnreadCount(userId);
        return new ResponseObject<>(count, "Lấy số lượng thông báo chưa đọc thành công");
    }
    
    /**
     * Clear all notifications for current user
     * POST /api/message/clear-all
     */
    @PostMapping("/clear-all")
    public ResponseObject<Boolean> clearAll() {
        Integer userId = getCurrentUserId();
        notificationService.clearAllForUser(userId);
        return new ResponseObject<>(true, "Xóa tất cả thông báo thành công");
    }
    
    /**
     * Create a test notification (for testing real-time notifications)
     * POST /api/message/test-notification
     * Body: { "userId": 1, "type": "message", "title": "Test", "content": "Test content" }
     */
    @PostMapping("/test-notification")
    public ResponseObject<NotificationResponse> createTestNotification(@RequestBody Map<String, Object> request) {
        Integer userId = request.get("userId") != null 
            ? Integer.parseInt(request.get("userId").toString()) 
            : getCurrentUserId();
        
        String type = request.getOrDefault("type", "notice").toString();
        String title = request.getOrDefault("title", "Thông báo thử nghiệm").toString();
        String subTitle = request.getOrDefault("subTitle", "Hệ thống").toString();
        String content = request.getOrDefault("content", "Đây là thông báo thử nghiệm real-time").toString();
        Integer messageType = request.get("messageType") != null
            ? Integer.parseInt(request.get("messageType").toString())
            : 3;
        
        Notification notification = notificationService.createNotification(
            userId, type, title, subTitle, content, messageType
        );
        
        return new ResponseObject<>(NotificationResponse.fromEntity(notification), "Tạo thông báo thử nghiệm thành công");
    }
    
    /**
     * Get user notification preferences
     * GET /api/message/preferences
     */
    @GetMapping("/preferences")
    public ResponseObject<NotificationPreference> getPreferences() {
        Integer userId = getCurrentUserId();
        NotificationPreference prefs = preferenceService.getOrCreatePreferences(userId);
        return new ResponseObject<>(prefs, "Lấy cài đặt thông báo thành công");
    }
    
    /**
     * Update user notification preferences
     * PUT /api/message/preferences
     */
    @PutMapping("/preferences")
    public ResponseObject<NotificationPreference> updatePreferences(
            @RequestBody NotificationPreference request) {
        Integer userId = getCurrentUserId();
        NotificationPreference updated = preferenceService.updatePreferences(userId, request);
        return new ResponseObject<>(updated, "Cập nhật cài đặt thông báo thành công");
    }
}
