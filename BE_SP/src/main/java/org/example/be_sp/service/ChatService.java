package org.example.be_sp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.CuocTraoDoi;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.TinNhan;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.CuocTraoDoiResponse;
import org.example.be_sp.model.response.TinNhanResponse;
import org.example.be_sp.repository.CuocTraoDoiRepository;
import org.example.be_sp.repository.NhanVienRepository;
import org.example.be_sp.repository.TinNhanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service xử lý logic nghiệp vụ cho chat
 */
@Service
public class ChatService {

    private final TinNhanRepository tinNhanRepository;
    private final CuocTraoDoiRepository cuocTraoDoiRepository;
    private final NhanVienRepository nhanVienRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(
            TinNhanRepository tinNhanRepository,
            CuocTraoDoiRepository cuocTraoDoiRepository,
            NhanVienRepository nhanVienRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.tinNhanRepository = tinNhanRepository;
        this.cuocTraoDoiRepository = cuocTraoDoiRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Lấy danh sách cuộc trò chuyện của người dùng
     */
    public List<CuocTraoDoiResponse> getConversationsByUserId(Integer userId) {
        return cuocTraoDoiRepository.findAllConversationsByUser(userId)
                .stream()
                .map(CuocTraoDoiResponse::new)
                .toList();
    }

    /**
     * Lấy danh sách tin nhắn giữa 2 người dùng (có phân trang)
     */
    public Page<TinNhanResponse> getMessagesBetweenUsers(Integer userId1, Integer userId2, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return tinNhanRepository.findMessagesBetweenUsers(userId1, userId2, pageable)
                .map(TinNhanResponse::new);
    }

    /**
     * Gửi tin nhắn mới
     */
    @Transactional
    public TinNhanResponse sendMessage(Integer senderId, SendMessageRequest request) {
        // Kiểm tra sender và receiver tồn tại
        NhanVien sender = nhanVienRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy người gửi", "404"));
        NhanVien receiver = nhanVienRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ApiException("Không tìm thấy người nhận", "404"));

        // Tạo tin nhắn mới
        TinNhan tinNhan = new TinNhan();
        tinNhan.setNguoiGui(sender);
        tinNhan.setNguoiNhan(receiver);
        tinNhan.setNoiDung(request.getContent());
        tinNhan.setLoaiTinNhan(request.getMessageType());
        tinNhan.setDaDoc(false);
        tinNhan.setThoiGianGui(LocalDateTime.now());
        tinNhan.setTrangThai(true);
        tinNhan.setDeleted(false);
        tinNhan.setCreateAt(LocalDateTime.now());
        tinNhan.setCreateBy(senderId);

        TinNhan savedMessage = tinNhanRepository.save(tinNhan);

        // Cập nhật hoặc tạo cuộc trò chuyện
        updateOrCreateConversation(senderId, request.getReceiverId(), request.getContent());

        // Convert to response for WebSocket notification
        TinNhanResponse messageResponse = new TinNhanResponse(savedMessage);

        // Gửi tin nhắn real-time tới receiver qua WebSocket (dùng username)
        try {
            String receiverUsername = receiver.getTenTaiKhoan();
            String senderUsername = sender.getTenTaiKhoan();
            
            // Send to receiver
            messagingTemplate.convertAndSendToUser(
                receiverUsername,
                "/queue/messages",
                messageResponse
            );
            
            // Also send confirmation to sender (for consistency)
            messagingTemplate.convertAndSendToUser(
                senderUsername,
                "/queue/messages",
                messageResponse
            );
        } catch (Exception e) {
            // Log error but don't fail the request if WebSocket fails
            System.err.println("Error sending WebSocket notification: " + e.getMessage());
        }

        return messageResponse;
    }

    /**
     * Đánh dấu tin nhắn là đã đọc
     */
    @Transactional
    public void markMessagesAsRead(Integer senderId, Integer receiverId) {
        // Cập nhật tất cả tin nhắn từ sender tới receiver thành đã đọc
        tinNhanRepository.markMessagesAsRead(senderId, receiverId);

        // Cập nhật unread count trong conversation
        Optional<CuocTraoDoi> conversationOpt = cuocTraoDoiRepository
                .findConversationBetweenUsers(senderId, receiverId);

        if (conversationOpt.isPresent()) {
            CuocTraoDoi conversation = conversationOpt.get();
            // Reset unread count cho receiver
            if (conversation.getNhanVien1().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv1(0);
            } else if (conversation.getNhanVien2().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv2(0);
            }
            conversation.setUpdateAt(LocalDateTime.now());
            conversation.setUpdateBy(receiverId);
            cuocTraoDoiRepository.save(conversation);
            
            // Gửi thông báo đã đọc qua WebSocket cho sender
            try {
                NhanVien sender = nhanVienRepository.findById(senderId)
                    .orElse(null);
                    
                if (sender != null) {
                    Map<String, Object> readNotification = new HashMap<>();
                    readNotification.put("senderId", senderId);
                    readNotification.put("receiverId", receiverId);
                    readNotification.put("readAt", LocalDateTime.now().toString());
                    
                    String senderUsername = sender.getTenTaiKhoan();
                    
                    messagingTemplate.convertAndSendToUser(
                        senderUsername,
                        "/queue/read",
                        readNotification
                    );
                }
            } catch (Exception e) {
                System.err.println("❌ Error sending read notification: " + e.getMessage());
            }
        }
    }

    /**
     * Đếm tổng số tin nhắn chưa đọc của người dùng
     */
    public Long getTotalUnreadCount(Integer userId) {
        return cuocTraoDoiRepository.countTotalUnreadMessagesForUser(userId);
    }

    /**
     * Lấy hoặc tạo cuộc trò chuyện giữa 2 người dùng
     */
    public CuocTraoDoiResponse getOrCreateConversation(Integer userId1, Integer userId2) {
        Optional<CuocTraoDoi> existingConversation = cuocTraoDoiRepository
                .findConversationBetweenUsers(userId1, userId2);

        if (existingConversation.isPresent()) {
            return new CuocTraoDoiResponse(existingConversation.get());
        }

        // Tạo cuộc trò chuyện mới
        NhanVien nv1 = nhanVienRepository.findById(userId1)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên 1", "404"));
        NhanVien nv2 = nhanVienRepository.findById(userId2)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên 2", "404"));

        CuocTraoDoi cuocTraoDoi = new CuocTraoDoi();
        cuocTraoDoi.setNhanVien1(nv1);
        cuocTraoDoi.setNhanVien2(nv2);
        cuocTraoDoi.setSoTinNhanChuaDocNv1(0);
        cuocTraoDoi.setSoTinNhanChuaDocNv2(0);
        cuocTraoDoi.setTrangThai(true);
        cuocTraoDoi.setDeleted(false);
        cuocTraoDoi.setCreateAt(LocalDateTime.now());
        cuocTraoDoi.setCreateBy(userId1);

        CuocTraoDoi saved = cuocTraoDoiRepository.save(cuocTraoDoi);
        return new CuocTraoDoiResponse(saved);
    }

    /**
     * Cập nhật hoặc tạo cuộc trò chuyện khi có tin nhắn mới
     */
    @Transactional
    protected void updateOrCreateConversation(Integer senderId, Integer receiverId, String messageContent) {
        Optional<CuocTraoDoi> conversationOpt = cuocTraoDoiRepository
                .findConversationBetweenUsers(senderId, receiverId);

        CuocTraoDoi conversation;
        if (conversationOpt.isPresent()) {
            conversation = conversationOpt.get();
        } else {
            // Tạo cuộc trò chuyện mới
            NhanVien sender = nhanVienRepository.getById(senderId);
            NhanVien receiver = nhanVienRepository.getById(receiverId);
            
            conversation = new CuocTraoDoi();
            conversation.setNhanVien1(sender);
            conversation.setNhanVien2(receiver);
            conversation.setSoTinNhanChuaDocNv1(0);
            conversation.setSoTinNhanChuaDocNv2(0);
            conversation.setTrangThai(true);
            conversation.setDeleted(false);
            conversation.setCreateAt(LocalDateTime.now());
            conversation.setCreateBy(senderId);
        }

        // Cập nhật thông tin tin nhắn cuối
        conversation.setTinNhanCuoiCung(messageContent.length() > 500 ? 
                messageContent.substring(0, 500) : messageContent);
        conversation.setThoiGianTinNhanCuoi(LocalDateTime.now());
        conversation.setNguoiGuiCuoi(nhanVienRepository.getById(senderId));

        // Tăng unread count cho receiver
        if (conversation.getNhanVien1().getId().equals(receiverId)) {
            conversation.setSoTinNhanChuaDocNv1(conversation.getSoTinNhanChuaDocNv1() + 1);
        } else if (conversation.getNhanVien2().getId().equals(receiverId)) {
            conversation.setSoTinNhanChuaDocNv2(conversation.getSoTinNhanChuaDocNv2() + 1);
        }

        conversation.setUpdateAt(LocalDateTime.now());
        conversation.setUpdateBy(senderId);
        
        cuocTraoDoiRepository.save(conversation);
    }
}
