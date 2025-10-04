package org.example.be_sp.controller;

import lombok.RequiredArgsConstructor;
import org.example.be_sp.annotation.RequireAuth;
import org.example.be_sp.model.response.NotificationResponse;
import org.example.be_sp.model.response.ResponseObject;
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
}
