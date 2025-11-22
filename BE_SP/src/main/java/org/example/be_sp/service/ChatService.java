package org.example.be_sp.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.example.be_sp.entity.AiChatHistory;
import org.example.be_sp.entity.CuocTraoDoi;
import org.example.be_sp.entity.KhachHang;
import org.example.be_sp.entity.NhanVien;
import org.example.be_sp.entity.TinNhan;
import org.example.be_sp.exception.ApiException;
import org.example.be_sp.model.request.SendMessageRequest;
import org.example.be_sp.model.response.AiChatHistoryResponse;
import org.example.be_sp.model.response.CuocTraoDoiResponse;
import org.example.be_sp.model.response.TinNhanResponse;
import org.example.be_sp.repository.AiChatHistoryRepository;
import org.example.be_sp.repository.CuocTraoDoiRepository;
import org.example.be_sp.repository.KhachHangRepository;
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
    private final KhachHangRepository khachHangRepository;
    private final AiChatHistoryRepository aiChatHistoryRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(
            TinNhanRepository tinNhanRepository,
            CuocTraoDoiRepository cuocTraoDoiRepository,
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository,
            AiChatHistoryRepository aiChatHistoryRepository,
            SimpMessagingTemplate messagingTemplate) {
        this.tinNhanRepository = tinNhanRepository;
        this.cuocTraoDoiRepository = cuocTraoDoiRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
        this.aiChatHistoryRepository = aiChatHistoryRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Xác định user có phải là customer không
     * Returns true only if the user exists in KhachHang and NOT in NhanVien
     */
    private boolean isCustomer(Integer userId) {
        boolean existsInKhachHang = khachHangRepository.findById(userId).isPresent();
        boolean existsInNhanVien = nhanVienRepository.findById(userId).isPresent();
        
        // If exists in both, prioritize staff (NhanVien)
        if (existsInKhachHang && existsInNhanVien) {
            System.out.println("⚠️ Warning: User ID " + userId + " exists in both KhachHang and NhanVien! Treating as staff.");
            return false;
        }
        
        return existsInKhachHang && !existsInNhanVien;
    }

    /**
     * Lấy username từ userId (có thể là customer hoặc staff)
     */
    private String getUsername(Integer userId) {
        Optional<KhachHang> khachHang = khachHangRepository.findById(userId);
        if (khachHang.isPresent()) {
            return khachHang.get().getTenTaiKhoan() != null 
                ? khachHang.get().getTenTaiKhoan() 
                : khachHang.get().getEmail();
        }
        Optional<NhanVien> nhanVien = nhanVienRepository.findById(userId);
        if (nhanVien.isPresent()) {
            return nhanVien.get().getTenTaiKhoan();
        }
        throw new ApiException("Không tìm thấy người dùng với ID: " + userId, "404");
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
     * Gửi tin nhắn mới (hỗ trợ cả customer và staff)
     */
    @Transactional
    public TinNhanResponse sendMessage(Integer senderId, SendMessageRequest request) {
        boolean senderIsCustomer = isCustomer(senderId);
        boolean receiverIsCustomer = isCustomer(request.getReceiverId());
        
        // Debug logging
        System.out.println("📨 sendMessage called:");
        System.out.println("   Sender ID: " + senderId + " (isCustomer: " + senderIsCustomer + ")");
        System.out.println("   Receiver ID: " + request.getReceiverId() + " (isCustomer: " + receiverIsCustomer + ")");
        
        // Xác định loại tin nhắn
        String messageType = (senderIsCustomer || receiverIsCustomer) ? "CUSTOMER_STAFF" : "STAFF_STAFF";
        System.out.println("   Message type: " + messageType);
        
        // Tạo tin nhắn mới
        TinNhan tinNhan = new TinNhan();
        tinNhan.setNoiDung(request.getContent());
        tinNhan.setLoaiTinNhan(request.getMessageType());
        tinNhan.setLoaiTinNhanType(messageType);
        tinNhan.setDaDoc(false);
        tinNhan.setThoiGianGui(LocalDateTime.now());
        tinNhan.setTrangThai(true);
        tinNhan.setDeleted(false);
        tinNhan.setCreateAt(LocalDateTime.now());
        tinNhan.setCreateBy(senderId);

        // Set sender và receiver dựa trên loại
        if (messageType.equals("CUSTOMER_STAFF")) {
            if (senderIsCustomer) {
                // Customer sending to staff
                KhachHang khachHang = khachHangRepository.findById(senderId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng gửi", "404"));
                NhanVien nhanVien = nhanVienRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên nhận", "404"));
                tinNhan.setKhachHangGui(khachHang);
                tinNhan.setNguoiNhan(nhanVien);
            } else {
                // Staff sending to customer
                NhanVien nhanVien = nhanVienRepository.findById(senderId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên gửi", "404"));
                KhachHang khachHang = khachHangRepository.findById(request.getReceiverId())
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng nhận", "404"));
                tinNhan.setNguoiGui(nhanVien);
                tinNhan.setKhachHangNhan(khachHang);
            }
        } else {
            // STAFF_STAFF
            NhanVien sender = nhanVienRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy người gửi", "404"));
            NhanVien receiver = nhanVienRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ApiException("Không tìm thấy người nhận", "404"));
            tinNhan.setNguoiGui(sender);
            tinNhan.setNguoiNhan(receiver);
        }

        TinNhan savedMessage = tinNhanRepository.save(tinNhan);

        // Cập nhật hoặc tạo cuộc trò chuyện
        updateOrCreateConversation(senderId, request.getReceiverId(), request.getContent(), messageType);

        // Convert to response for WebSocket notification
        TinNhanResponse messageResponse = new TinNhanResponse(savedMessage);

        // Gửi tin nhắn real-time tới receiver qua WebSocket (dùng username)
        try {
            String receiverUsername = getUsername(request.getReceiverId());
            String senderUsername = getUsername(senderId);
            
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
     * Đánh dấu tin nhắn là đã đọc (hỗ trợ cả customer và staff)
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
            if (conversation.getLoaiCuocTraoDoi() != null && conversation.getLoaiCuocTraoDoi().equals("CUSTOMER_STAFF")) {
                // Customer-staff conversation
                if (conversation.getKhachHang() != null && conversation.getKhachHang().getId().equals(receiverId)) {
                    conversation.setSoTinNhanChuaDocNv1(0);
                } else if (conversation.getNhanVien() != null && conversation.getNhanVien().getId().equals(receiverId)) {
                    conversation.setSoTinNhanChuaDocNv2(0);
                }
            } else {
                // STAFF_STAFF
                if (conversation.getNhanVien1() != null && conversation.getNhanVien1().getId().equals(receiverId)) {
                    conversation.setSoTinNhanChuaDocNv1(0);
                } else if (conversation.getNhanVien2() != null && conversation.getNhanVien2().getId().equals(receiverId)) {
                    conversation.setSoTinNhanChuaDocNv2(0);
                }
            }
            conversation.setUpdateAt(LocalDateTime.now());
            conversation.setUpdateBy(receiverId);
            cuocTraoDoiRepository.save(conversation);
            
            // Gửi thông báo đã đọc qua WebSocket cho sender
            try {
                String senderUsername = getUsername(senderId);
                
                Map<String, Object> readNotification = new HashMap<>();
                readNotification.put("senderId", senderId);
                readNotification.put("receiverId", receiverId);
                readNotification.put("readAt", LocalDateTime.now().toString());
                
                messagingTemplate.convertAndSendToUser(
                    senderUsername,
                    "/queue/read",
                    readNotification
                );
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
     * Lấy hoặc tạo cuộc trò chuyện giữa 2 người dùng (hỗ trợ cả customer và staff)
     */
    public CuocTraoDoiResponse getOrCreateConversation(Integer userId1, Integer userId2) {
        Optional<CuocTraoDoi> existingConversation = cuocTraoDoiRepository
                .findConversationBetweenUsers(userId1, userId2);

        if (existingConversation.isPresent()) {
            return new CuocTraoDoiResponse(existingConversation.get());
        }

        boolean user1IsCustomer = isCustomer(userId1);
        boolean user2IsCustomer = isCustomer(userId2);
        String conversationType = (user1IsCustomer || user2IsCustomer) ? "CUSTOMER_STAFF" : "STAFF_STAFF";

        CuocTraoDoi cuocTraoDoi = new CuocTraoDoi();
        cuocTraoDoi.setLoaiCuocTraoDoi(conversationType);
        cuocTraoDoi.setSoTinNhanChuaDocNv1(0);
        cuocTraoDoi.setSoTinNhanChuaDocNv2(0);
        cuocTraoDoi.setTrangThai(true);
        cuocTraoDoi.setDeleted(false);
        cuocTraoDoi.setCreateAt(LocalDateTime.now());
        cuocTraoDoi.setCreateBy(userId1);

        if (conversationType.equals("CUSTOMER_STAFF")) {
            if (user1IsCustomer) {
                KhachHang kh = khachHangRepository.findById(userId1)
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
                NhanVien nv = nhanVienRepository.findById(userId2)
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));
                cuocTraoDoi.setKhachHang(kh);
                cuocTraoDoi.setNhanVien(nv);
            } else {
                NhanVien nv = nhanVienRepository.findById(userId1)
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));
                KhachHang kh = khachHangRepository.findById(userId2)
                    .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
                cuocTraoDoi.setNhanVien(nv);
                cuocTraoDoi.setKhachHang(kh);
            }
        } else {
            // STAFF_STAFF
            NhanVien nv1 = nhanVienRepository.findById(userId1)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên 1", "404"));
            NhanVien nv2 = nhanVienRepository.findById(userId2)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên 2", "404"));
            cuocTraoDoi.setNhanVien1(nv1);
            cuocTraoDoi.setNhanVien2(nv2);
        }

        CuocTraoDoi saved = cuocTraoDoiRepository.save(cuocTraoDoi);
        return new CuocTraoDoiResponse(saved);
    }

    /**
     * Cập nhật hoặc tạo cuộc trò chuyện khi có tin nhắn mới (hỗ trợ cả customer và staff)
     */
    @Transactional
    protected void updateOrCreateConversation(Integer senderId, Integer receiverId, String messageContent, String conversationType) {
        Optional<CuocTraoDoi> conversationOpt = cuocTraoDoiRepository
                .findConversationBetweenUsers(senderId, receiverId);

        CuocTraoDoi conversation;
        if (conversationOpt.isPresent()) {
            conversation = conversationOpt.get();
        } else {
            // Tạo cuộc trò chuyện mới
            boolean senderIsCustomer = isCustomer(senderId);
            
            conversation = new CuocTraoDoi();
            conversation.setLoaiCuocTraoDoi(conversationType);
            conversation.setSoTinNhanChuaDocNv1(0);
            conversation.setSoTinNhanChuaDocNv2(0);
            conversation.setTrangThai(true);
            conversation.setDeleted(false);
            conversation.setCreateAt(LocalDateTime.now());
            conversation.setCreateBy(senderId);

            if (conversationType.equals("CUSTOMER_STAFF")) {
                if (senderIsCustomer) {
                    KhachHang kh = khachHangRepository.findById(senderId)
                        .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
                    NhanVien nv = nhanVienRepository.findById(receiverId)
                        .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));
                    conversation.setKhachHang(kh);
                    conversation.setNhanVien(nv);
                } else {
                    NhanVien nv = nhanVienRepository.findById(senderId)
                        .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));
                    KhachHang kh = khachHangRepository.findById(receiverId)
                        .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
                    conversation.setNhanVien(nv);
                    conversation.setKhachHang(kh);
                }
            } else {
                // STAFF_STAFF
                NhanVien sender = nhanVienRepository.findById(senderId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên gửi", "404"));
                NhanVien receiver = nhanVienRepository.findById(receiverId)
                    .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên nhận", "404"));
                conversation.setNhanVien1(sender);
                conversation.setNhanVien2(receiver);
            }
        }

        // Cập nhật thông tin tin nhắn cuối
        conversation.setTinNhanCuoiCung(messageContent.length() > 500 ? 
                messageContent.substring(0, 500) : messageContent);
        conversation.setThoiGianTinNhanCuoi(LocalDateTime.now());
        
        // Set người gửi cuối
        boolean senderIsCustomer = isCustomer(senderId);
        if (senderIsCustomer) {
            KhachHang kh = khachHangRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
            conversation.setKhachHangGuiCuoi(kh);
        } else {
            NhanVien nv = nhanVienRepository.findById(senderId)
                .orElseThrow(() -> new ApiException("Không tìm thấy nhân viên", "404"));
            conversation.setNguoiGuiCuoi(nv);
        }

        // Tăng unread count cho receiver
        if (conversation.getLoaiCuocTraoDoi() != null && conversation.getLoaiCuocTraoDoi().equals("CUSTOMER_STAFF")) {
            // Customer-staff conversation: chỉ có 1 unread count field
            if (conversation.getKhachHang() != null && conversation.getKhachHang().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv1(conversation.getSoTinNhanChuaDocNv1() + 1);
            } else if (conversation.getNhanVien() != null && conversation.getNhanVien().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv2(conversation.getSoTinNhanChuaDocNv2() + 1);
            }
        } else {
            // STAFF_STAFF
            if (conversation.getNhanVien1() != null && conversation.getNhanVien1().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv1(conversation.getSoTinNhanChuaDocNv1() + 1);
            } else if (conversation.getNhanVien2() != null && conversation.getNhanVien2().getId().equals(receiverId)) {
                conversation.setSoTinNhanChuaDocNv2(conversation.getSoTinNhanChuaDocNv2() + 1);
            }
        }

        conversation.setUpdateAt(LocalDateTime.now());
        conversation.setUpdateBy(senderId);
        
        cuocTraoDoiRepository.save(conversation);
    }

    /**
     * Lấy lịch sử chat AI của khách hàng (chỉ lấy session gần nhất)
     */
    public List<AiChatHistoryResponse> getCustomerAiChatHistory(Integer customerId) {
        try {
            // First check if customer has any history
            Long totalCount = aiChatHistoryRepository.countByCustomerId(customerId);
            //System.out.println("📊 Total AI chat history count for customer " + customerId + ": " + totalCount);
            
            if (totalCount == null || totalCount == 0) {
                System.out.println("⚠️ No AI chat history found for customer " + customerId);
                return List.of();
            }
            
            // Get the most recent session ID
            List<String> sessionIds = aiChatHistoryRepository.findMostRecentSessionIds(customerId);
            
            if (sessionIds == null || sessionIds.isEmpty()) {
                //System.out.println("⚠️ No session ID found for customer " + customerId);
                return List.of();
            }
            
            String mostRecentSessionId = sessionIds.get(0);
            if (mostRecentSessionId == null || mostRecentSessionId.trim().isEmpty()) {
                //System.out.println("⚠️ Session ID is empty for customer " + customerId);
                return List.of();
            }
            
            //System.out.println("✅ Loading AI chat history for customer " + customerId + ", session: " + mostRecentSessionId);
            
            // Get all messages from the most recent session
            List<AiChatHistory> history = aiChatHistoryRepository.findByCustomerIdAndSessionId(customerId, mostRecentSessionId);
            //System.out.println("✅ Found " + history.size() + " messages in session " + mostRecentSessionId);
            
            return history.stream()
                .map(AiChatHistoryResponse::new)
                .toList();
        } catch (Exception e) {
            //System.err.println("❌ Error in getCustomerAiChatHistory for customer " + customerId + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Lưu lịch sử chat AI của khách hàng
     */
    @Transactional
    public AiChatHistory saveAiChatHistory(Integer customerId, String sessionId, String role, String content) {
        KhachHang khachHang = khachHangRepository.findById(customerId)
            .orElseThrow(() -> new ApiException("Không tìm thấy khách hàng", "404"));
        
        AiChatHistory history = new AiChatHistory();
        history.setKhachHang(khachHang);
        history.setSessionId(sessionId);
        history.setRole(role);
        history.setContent(content);
        history.setTimestamp(LocalDateTime.now());
        history.setCreatedAt(LocalDateTime.now());
        history.setCreatedBy(customerId);
        
        return aiChatHistoryRepository.save(history);
    }
}
