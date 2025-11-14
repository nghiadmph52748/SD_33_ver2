package org.example.be_sp.controller;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.SaveAiChatHistoryRequest;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.service.ChatService;
import org.example.be_sp.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
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
     * Lấy lịch sử chat AI của khách hàng (chỉ staff mới có quyền)
     */
    @GetMapping("/ai-history/{customerId}")
    public ResponseObject<?> getCustomerAiChatHistory(@PathVariable Integer customerId) {
        try {
            // Verify current user is staff (not customer)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isCustomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equalsIgnoreCase("ROLE_CUSTOMER"));
            
            if (isCustomer) {
                return new ResponseObject<>(false, null, "Chỉ nhân viên mới có quyền xem lịch sử AI chat");
            }
            
            var history = chatService.getCustomerAiChatHistory(customerId);
            System.out.println("✅ Loaded AI chat history for customer " + customerId + ": " + history.size() + " messages");
            return new ResponseObject<>(true, 
                history, 
                "Lấy lịch sử AI chat thành công");
        } catch (Exception e) {
            System.err.println("❌ Error loading AI chat history for customer " + customerId + ": " + e.getMessage());
            e.printStackTrace();
            return new ResponseObject<>(false, null, 
                "Lỗi khi lấy lịch sử AI chat: " + e.getMessage());
        }
    }

    /**
     * Lưu lịch sử chat AI (customer có thể tự lưu, staff cũng có thể lưu cho customer)
     */
    @PostMapping("/ai-history")
    public ResponseObject<?> saveAiChatHistory(@Valid @RequestBody SaveAiChatHistoryRequest request) {
        try {
            Integer currentUserId = getCurrentUserId();
            
            // Verify that the customer ID matches the current user (if customer) or allow staff to save for any customer
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isCustomer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equalsIgnoreCase("ROLE_CUSTOMER"));
            
            // If current user is customer, they can only save their own history
            if (isCustomer && !currentUserId.equals(request.getCustomerId())) {
                return new ResponseObject<>(false, null, "Bạn chỉ có thể lưu lịch sử AI chat của chính mình");
            }
            
            var saved = chatService.saveAiChatHistory(
                request.getCustomerId(),
                request.getSessionId(),
                request.getRole(),
                request.getContent()
            );
            
            return new ResponseObject<>(true, 
                saved, 
                "Lưu lịch sử AI chat thành công");
        } catch (Exception e) {
            return new ResponseObject<>(false, null, 
                "Lỗi khi lưu lịch sử AI chat: " + e.getMessage());
        }
    }

    /**
     * Lấy ID của người dùng hiện tại từ Security Context
     * Hỗ trợ cả customer và staff
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Đăng nhập lại");
        }
        
        String username = authentication.getName();
        
        // Kiểm tra role từ authentication authorities để xác định user type
        boolean isCustomer = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equalsIgnoreCase("ROLE_CUSTOMER"));
        
        // Nếu là customer, tìm trong KhachHang
        if (isCustomer) {
            KhachHang khachHang = khachHangRepository.findByTenTaiKhoan(username);
            if (khachHang != null) {
                return khachHang.getId();
            }
            // Fallback: thử tìm theo email
            khachHang = khachHangRepository.findByEmail(username);
            if (khachHang != null) {
                return khachHang.getId();
            }
            throw new RuntimeException("Không tìm thấy khách hàng: " + username);
        }
        
        // Nếu là staff, tìm trong NhanVien
        NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy nhân viên: " + username);
        }
        
        return nhanVien.getId();
    }
}
