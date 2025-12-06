package org.example.be_sp.controller;

import java.security.Principal;
import java.util.Optional;

import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.TinNhanResponse;
import org.example.be_sp.repository.KhachHangRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.service.ChatService;
import org.example.be_sp.service.NhanVienService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller để xử lý tin nhắn real-time
 */
@Controller
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NhanVienService nhanVienService;
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;

    public ChatWebSocketController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate,
            NhanVienService nhanVienService,
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.nhanVienService = nhanVienService;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
    }

    /**
     * Xử lý tin nhắn được gửi từ client qua WebSocket
     * Client gửi tới: /app/chat.send
     * Server phát tin nhắn tới: /user/{receiverId}/queue/messages
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        try {
            // Lấy senderId từ authenticated user
            Integer senderId = getUserIdFromPrincipal(principal);
            String senderUsername = principal.getName();
            
            // Handle guest users
            if (senderId == null && senderUsername != null && senderUsername.startsWith("guest-")) {
                // Guest user - assign an available staff member automatically
                Integer assignedStaffId = chatService.assignStaffForGuest(request);
                if (assignedStaffId == null) {
                    throw new RuntimeException("Hiện tại không có nhân viên nào đang hoạt động. Vui lòng thử lại sau.");
                }
                
                // Create guest message with assigned staff
                TinNhanResponse savedMessage = chatService.sendGuestMessage(senderUsername, assignedStaffId, request);
                
                // Get receiver username
                String receiverUsername = getUsernameFromUserId(assignedStaffId);
                
                // Send to assigned staff
                messagingTemplate.convertAndSendToUser(
                    receiverUsername,
                    "/queue/messages",
                    savedMessage
                );
                
                // Send confirmation to guest (using guest session ID)
                messagingTemplate.convertAndSendToUser(
                    senderUsername,
                    "/queue/messages",
                    savedMessage
                );
                
                System.out.println("✅ Guest WebSocket message sent successfully:");
                System.out.println("   From: Guest (" + senderUsername + ")");
                System.out.println("   To: " + receiverUsername + " (ID: " + assignedStaffId + ")");
                System.out.println("   Content: " + savedMessage.getContent().substring(0, Math.min(50, savedMessage.getContent().length())));
                return;
            }
            
            // Regular authenticated user flow
            // Lưu tin nhắn vào database
            TinNhanResponse savedMessage = chatService.sendMessage(senderId, request);
            
            // Lấy username của receiver từ userId (có thể là customer hoặc staff)
            String receiverUsername = getUsernameFromUserId(request.getReceiverId());
            
            // Gửi tin nhắn real-time tới receiver qua WebSocket (dùng username)
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/messages",
                savedMessage
            );
            
            // Cũng gửi lại cho sender để confirm (dùng username)
            messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/messages",
                savedMessage
            );
            
            System.out.println("✅ WebSocket message sent successfully:");
            System.out.println("   From: " + senderUsername + " (ID: " + senderId + ")");
            System.out.println("   To: " + receiverUsername + " (ID: " + request.getReceiverId() + ")");
            System.out.println("   Content: " + savedMessage.getContent().substring(0, Math.min(50, savedMessage.getContent().length())));
            
        } catch (Exception e) {
            // Log error for debugging
            System.err.println("❌ Error in ChatWebSocketController.sendMessage:");
            System.err.println("   Sender: " + (principal != null ? principal.getName() : "null"));
            System.err.println("   Receiver ID: " + request.getReceiverId());
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thông báo khi người dùng đang đánh tin nhắn
     * Client gửi tới: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void notifyTyping(@Payload TypingNotification notification, Principal principal) {
        try {
            // Gửi thông báo typing tới người nhận
            messagingTemplate.convertAndSendToUser(
                notification.getReceiverId().toString(),
                "/queue/typing",
                notification
            );
        } catch (Exception e) {
            // Silently handle error
        }
    }

    /**
     * Lấy userId từ Principal (hỗ trợ cả customer, staff và guest)
     */
    private Integer getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Đăng nhập lại");
        }
        
        String username = principal.getName();
        
        // Check if this is a guest session
        if (username != null && username.startsWith("guest-")) {
            // Guest user - return null to indicate guest
            // Service will handle guest message creation
            return null;
        }
        
        // Thử tìm trong NhanVien trước
        NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);
        if (nhanVien != null) {
            return nhanVien.getId();
        }
        
        // Nếu không tìm thấy, thử tìm trong KhachHang
        KhachHang khachHang = khachHangRepository.findByTenTaiKhoan(username);
        if (khachHang != null) {
            return khachHang.getId();
        }
        
        // Fallback: thử tìm theo email
        khachHang = khachHangRepository.findByEmail(username);
        if (khachHang != null) {
            return khachHang.getId();
        }
        
        throw new RuntimeException("Không tìm thấy người dùng: " + username);
    }
    
    /**
     * Lấy username từ userId (hỗ trợ cả customer và staff)
     */
    private String getUsernameFromUserId(Integer userId) {
        // Thử tìm trong NhanVien trước
        Optional<NhanVien> nhanVien = nhanVienRepository.findById(userId);
        if (nhanVien.isPresent()) {
            return nhanVien.get().getTenTaiKhoan();
        }
        
        // Nếu không tìm thấy, thử tìm trong KhachHang
        Optional<KhachHang> khachHang = khachHangRepository.findById(userId);
        if (khachHang.isPresent()) {
            KhachHang kh = khachHang.get();
            return kh.getTenTaiKhoan() != null ? kh.getTenTaiKhoan() : kh.getEmail();
        }
        
        throw new RuntimeException("Không tìm thấy người dùng với ID: " + userId);
    }

    /**
     * DTO đơn giản cho typing notification
     */
    public static class TypingNotification {
        private Integer senderId;
        private Integer receiverId;
        private Boolean isTyping;

        public Integer getSenderId() {
            return senderId;
        }

        public void setSenderId(Integer senderId) {
            this.senderId = senderId;
        }

        public Integer getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(Integer receiverId) {
            this.receiverId = receiverId;
        }

        public Boolean getIsTyping() {
            return isTyping;
        }

        public void setIsTyping(Boolean isTyping) {
            this.isTyping = isTyping;
        }
    }
}
