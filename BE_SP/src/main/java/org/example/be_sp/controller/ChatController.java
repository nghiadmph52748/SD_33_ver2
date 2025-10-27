package org.example.be_sp.controller;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.ChatService;
import org.example.be_sp.service.NhanVienService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * REST API Controller cho chức năng chat
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final NhanVienService nhanVienService;

    public ChatController(ChatService chatService, NhanVienService nhanVienService) {
        this.chatService = chatService;
        this.nhanVienService = nhanVienService;
    }

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng hiện tại
     */
    @GetMapping("/conversations")
    public ResponseObject<?> getConversations() {
        try {
            Integer userId = getCurrentUserId();
            var conversations = chatService.getConversationsByUserId(userId);
            return new ResponseObject<>(true, 
                conversations, 
                "Lấy danh sách cuộc trò chuyện thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi lấy danh sách cuộc trò chuyện: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách tin nhắn giữa 2 người dùng
     */
    @GetMapping("/messages/{otherUserId}")
    public ResponseObject<?> getMessages(
            @PathVariable Integer otherUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Integer userId = getCurrentUserId();
            return new ResponseObject<>(true, 
                chatService.getMessagesBetweenUsers(userId, otherUserId, page, size), 
                "Lấy danh sách tin nhắn thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi lấy danh sách tin nhắn: " + e.getMessage());
        }
    }

    /**
     * Gửi tin nhắn mới
     */
    @PostMapping("/messages")
    public ResponseObject<?> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        try {
            Integer senderId = getCurrentUserId();
            var result = chatService.sendMessage(senderId, request);
            return new ResponseObject<>(true, 
                result, 
                "Gửi tin nhắn thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi gửi tin nhắn: " + e.getMessage());
        }
    }

    /**
     * Đánh dấu tin nhắn là đã đọc
     */
    @PutMapping("/messages/read/{senderId}")
    public ResponseObject<?> markAsRead(@PathVariable Integer senderId) {
        try {
            Integer receiverId = getCurrentUserId();
            chatService.markMessagesAsRead(senderId, receiverId);
            return new ResponseObject<>(true, null, 
                "Đánh dấu tin nhắn đã đọc thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi đánh dấu tin nhắn: " + e.getMessage());
        }
    }

    /**
     * Lấy số lượng tin nhắn chưa đọc
     */
    @GetMapping("/unread-count")
    public ResponseObject<?> getUnreadCount() {
        try {
            Integer userId = getCurrentUserId();
            return new ResponseObject<>(true, 
                chatService.getTotalUnreadCount(userId), 
                "Lấy số tin nhắn chưa đọc thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi lấy số tin nhắn chưa đọc: " + e.getMessage());
        }
    }

    /**
     * Lấy hoặc tạo cuộc trò chuyện với người dùng khác
     */
    @GetMapping("/conversation/{otherUserId}")
    public ResponseObject<?> getOrCreateConversation(@PathVariable Integer otherUserId) {
        try {
            Integer userId = getCurrentUserId();
            return new ResponseObject<>(true, 
                chatService.getOrCreateConversation(userId, otherUserId), 
                "Lấy cuộc trò chuyện thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi lấy cuộc trò chuyện: " + e.getMessage());
        }
    }

    /**
     * Lấy ID của người dùng hiện tại từ Security Context
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Đạng nhập lại");
        }
        
        String username = authentication.getName();
        NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);
        
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy người dùng: " + username);
        }
        
        return nhanVien.getId();
    }
}
