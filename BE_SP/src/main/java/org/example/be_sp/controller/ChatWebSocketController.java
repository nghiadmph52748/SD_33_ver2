package org.example.be_sp.controller;

import java.security.Principal;

import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.TinNhanResponse;
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

    public ChatWebSocketController(
            ChatService chatService,
            SimpMessagingTemplate messagingTemplate,
            NhanVienService nhanVienService,
            NhanVienRepository nhanVienRepository) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.nhanVienService = nhanVienService;
        this.nhanVienRepository = nhanVienRepository;
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
            
            // Lưu tin nhắn vào database
            TinNhanResponse savedMessage = chatService.sendMessage(senderId, request);
            
            // Lấy username của receiver từ userId
            NhanVien receiver = nhanVienRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người nhận: " + request.getReceiverId()));
            String receiverUsername = receiver.getTenTaiKhoan();
            
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
            
        } catch (Exception e) {
            // Silently handle error
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
     * Lấy userId từ Principal
     */
    private Integer getUserIdFromPrincipal(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Đạng nhập lại");
        }
        
        String username = principal.getName();
        NhanVien nhanVien = nhanVienService.findByTenTaiKhoan(username);
        
        if (nhanVien == null) {
            throw new RuntimeException("Không tìm thấy người dùng: " + username);
        }
        
        return nhanVien.getId();
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
