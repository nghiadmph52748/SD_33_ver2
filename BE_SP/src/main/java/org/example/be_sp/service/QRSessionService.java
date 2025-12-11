package org.example.be_sp.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.QRSession;
import org.example.be_sp.model.request.CreateQRSessionRequest;
import org.example.be_sp.model.response.QRSessionEvent;
import org.example.be_sp.model.response.QRSessionResponse;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.QRSessionRepository;
import org.example.be_sp.service.upload.UploadImageToCloudinary;
import org.example.be_sp.util.QRGeneration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class QRSessionService {

    private static final int SESSION_TIMEOUT_MINUTES = 15;

    @Autowired
    private QRSessionRepository qrSessionRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UploadImageToCloudinary uploadImageToCloudinary;

    @Autowired(required = false)
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public QRSessionResponse createSession(CreateQRSessionRequest request) {
        try {
            HoaDon hoaDon = resolveHoaDon(request.getInvoiceId());
            Optional<QRSession> existingSession = findExistingSession(request, hoaDon);

            QRSession session = existingSession.orElseGet(QRSession::new);
            boolean isNewSession = session.getId() == null;

            if (isNewSession) {
                session.setSessionId(UUID.randomUUID().toString());
                session.setCreatedAt(LocalDateTime.now());
            }

            applyRequestDataToSession(session, request, hoaDon);

            QRSession saved = qrSessionRepository.save(session);
            publishSessionUpdate(saved);
            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Failed to create QR session", e);
            throw new RuntimeException("Failed to create QR session: " + e.getMessage(), e);
        }
    }

    public QRSessionResponse getSession(String sessionId) {
        QRSession session = qrSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getExpiresAt() != null && session.getExpiresAt().isBefore(LocalDateTime.now())
                && !"PAID".equals(session.getStatus())) {
            session.setStatus("EXPIRED");
            QRSession saved = qrSessionRepository.save(session);
            publishSessionUpdate(saved);
            session = saved;
        }

        return mapToDTO(session);
    }

    public QRSessionResponse getLatestActiveSession() {
        LocalDateTime now = LocalDateTime.now();
        // Only return sessions created in the last 5 minutes to avoid stale sessions after page reload
        LocalDateTime minCreatedAt = now.minusMinutes(5);

        while (true) {
            Optional<QRSession> sessionOpt = qrSessionRepository
                    .findFirstByStatusAndExpiresAtAfterAndCreatedAtAfterOrderByCreatedAtDesc("PENDING", now, minCreatedAt);

            if (sessionOpt.isEmpty()) {
                throw new RuntimeException("Không có phiên VNPAY đang hoạt động");
            }

            QRSession session = sessionOpt.get();

            if (isSessionUsable(session)) {
                return mapToDTO(session);
            }

            // Session không còn hợp lệ -> chuyển sang EXPIRED và tiếp tục tìm session khác
            session.setStatus("EXPIRED");
            session.setExpiresAt(now);
            qrSessionRepository.save(session);
            publishSessionUpdate(session);
        }
    }

    @Transactional
    public QRSessionResponse updateSession(String sessionId, CreateQRSessionRequest request) {
        try {
            QRSession session = qrSessionRepository.findBySessionId(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

            if ("PAID".equals(session.getStatus())) {
                throw new RuntimeException("Cannot update session: payment already completed");
            }

            HoaDon hoaDon = resolveHoaDon(request.getInvoiceId());
            // If invoice not supplied, keep existing
            if (hoaDon == null) {
                hoaDon = session.getIdHoaDon();
            }

            applyRequestDataToSession(session, request, hoaDon);

            QRSession saved = qrSessionRepository.save(session);
            publishSessionUpdate(saved);
            return mapToDTO(saved);
        } catch (Exception e) {
            log.error("Failed to update QR session {}", sessionId, e);
            throw new RuntimeException("Failed to update QR session: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateStatus(String sessionId, String status) {
        QRSession session = qrSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> {
                    log.error("Session not found: {}", sessionId);
                    return new RuntimeException("Session not found: " + sessionId);
                });
        session.setStatus(status);
        if (!"PAID".equals(status)) {
            session.setExpiresAt(LocalDateTime.now());
            clearSessionFinancials(session);
        }
        QRSession saved = qrSessionRepository.save(session);
        publishSessionUpdate(saved);
    }

    @Transactional
    public QRSessionResponse generateQrForSession(String sessionId) {
        QRSession session = qrSessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if ("PAID".equals(session.getStatus())) {
            throw new RuntimeException("Cannot generate QR for a paid session");
        }

        BigDecimal finalPrice = session.getFinalPrice();
        if (finalPrice == null || finalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Final price must be greater than zero to generate QR");
        }

        if (session.getQrCodeUrl() == null) {
            CreateQRSessionRequest request = new CreateQRSessionRequest();
            request.setOrderCode(session.getOrderCode());
            request.setFinalPrice(session.getFinalPrice());
            request.setSubtotal(session.getSubtotal());
            request.setDiscountAmount(session.getDiscountAmount());
            request.setShippingFee(session.getShippingFee());

            String qrUrl = generateQrUrl(session.getSessionId(), request);
            session.setQrCodeUrl(qrUrl);
            if (!"PAID".equals(session.getStatus())) {
                session.setStatus("PENDING");
            }
            QRSession saved = qrSessionRepository.save(session);
            log.info("Generated QR for existing session {}", session.getSessionId());
            publishSessionUpdate(saved);
            session = saved;
        }

        return mapToDTO(session);
    }

    private void applyRequestDataToSession(QRSession session, CreateQRSessionRequest request, HoaDon hoaDon)
            throws Exception {
        LocalDateTime now = LocalDateTime.now();
        session.setOrderCode(request.getOrderCode());
        if (hoaDon != null) {
            session.setIdHoaDon(hoaDon);
        }
        session.setExpiresAt(now.plusMinutes(SESSION_TIMEOUT_MINUTES));

        if (!"PAID".equals(session.getStatus())) {
            session.setStatus("PENDING");
        }

        String cartDataJson = objectMapper.writeValueAsString(request.getItems());
        session.setCartDataJson(cartDataJson);
        session.setSubtotal(request.getSubtotal());
        session.setDiscountAmount(request.getDiscountAmount());
        session.setShippingFee(request.getShippingFee());
        session.setFinalPrice(request.getFinalPrice());
    }

    private HoaDon resolveHoaDon(Integer invoiceId) {
        if (invoiceId == null) {
            return null;
        }
        return hoaDonRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
    }

    private Optional<QRSession> findExistingSession(CreateQRSessionRequest request, HoaDon hoaDon) {
        if (hoaDon != null) {
            return qrSessionRepository.findByIdHoaDon_Id(hoaDon.getId());
        }
        if (request.getOrderCode() != null) {
            return qrSessionRepository.findByOrderCode(request.getOrderCode());
        }
        return Optional.empty();
    }

    private String generateQrUrl(String sessionId, CreateQRSessionRequest request) {
        try {
            org.example.be_sp.model.request.VnpayCreatePaymentRequest vnpayRequest
                    = new org.example.be_sp.model.request.VnpayCreatePaymentRequest();
            vnpayRequest.setOrderId(request.getOrderCode());
            BigDecimal finalPrice = request.getFinalPrice();
            vnpayRequest.setAmount(finalPrice != null ? finalPrice.longValue() : 0L);
            vnpayRequest.setOrderInfo("Thanh toan don hang " + request.getOrderCode());
            vnpayRequest.setLocale("vn");

            String clientIp = "127.0.0.1";
            org.example.be_sp.model.response.VnpayCreatePaymentResponse vnpayResponse
                    = vnPayService.createPayment(vnpayRequest, clientIp, null);

            if (vnpayResponse != null && vnpayResponse.getPayUrl() != null) {
                // VNPAY payment URL when encoded as QR code IS the banking QR format
                // Banking apps (Vietcombank, BIDV, Techcombank, etc.) recognize VNPAY URLs
                // and can process payments directly from the QR code
                String payUrl = vnpayResponse.getPayUrl();
                byte[] qrCodeBytes = QRGeneration.generateQRCode(payUrl);
                return uploadQRCodeToCloudinary(qrCodeBytes);
            }
            throw new RuntimeException("VNPAY payment URL generation failed");
        } catch (Exception e) {
            log.error("VNPAY QR generation failed: {}. Cannot generate banking QR without VNPAY configuration.", e.getMessage());
            throw new RuntimeException("Không thể tạo mã QR thanh toán. Vui lòng cấu hình VNPAY (tmnCode/hashSecret) trong application.properties", e);
        }
    }

    private String uploadQRCodeToCloudinary(byte[] qrCodeBytes) {
        try {
            return uploadImageToCloudinary.uploadQrCode(qrCodeBytes);
        } catch (Exception e) {
            log.error("Failed to upload QR code to Cloudinary", e);
            throw new RuntimeException("Failed to upload QR code: " + e.getMessage(), e);
        }
    }

    private QRSessionResponse mapToDTO(QRSession session) {
        List<QRSessionResponse.CartItemResponse> items = null;
        try {
            if (session.getCartDataJson() != null) {
                List<CreateQRSessionRequest.CartItemDTO> cartItems = objectMapper.readValue(
                        session.getCartDataJson(),
                        objectMapper.getTypeFactory().constructCollectionType(List.class,
                                CreateQRSessionRequest.CartItemDTO.class));

                items = cartItems.stream().map(item -> QRSessionResponse.CartItemResponse.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .price(item.getPrice())
                        .discount(item.getDiscount())
                        .quantity(item.getQuantity())
                        .image(item.getImage())
                        .tenMauSac(item.getTenMauSac())
                        .maMau(item.getMaMau())
                        .tenKichThuoc(item.getTenKichThuoc())
                        .tenDeGiay(item.getTenDeGiay())
                        .tenChatLieu(item.getTenChatLieu())
                        .build()).collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed to parse cart data JSON", e);
        }

        String customerName = null;
        String customerPhone = null;
        String customerEmail = null;

        if (session.getIdHoaDon() != null) {
            HoaDon hoaDon = session.getIdHoaDon();
            if (hoaDon.getIdKhachHang() != null) {
                customerName = hoaDon.getIdKhachHang().getTenKhachHang();
                customerPhone = hoaDon.getIdKhachHang().getSoDienThoai();
                customerEmail = hoaDon.getIdKhachHang().getEmail();
            } else {
                customerName = hoaDon.getTenNguoiNhan();
                customerPhone = hoaDon.getSoDienThoaiNguoiNhan();
                customerEmail = hoaDon.getEmailNguoiNhan();
            }
        }

        return QRSessionResponse.builder()
                .qrSessionId(session.getSessionId())
                .qrCodeUrl(session.getQrCodeUrl())
                .orderCode(session.getOrderCode())
                .items(items)
                .subtotal(session.getSubtotal())
                .discountAmount(session.getDiscountAmount())
                .shippingFee(session.getShippingFee())
                .finalPrice(session.getFinalPrice())
                .status(session.getStatus())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .customerName(customerName)
                .customerPhone(customerPhone)
                .customerEmail(customerEmail)
                .build();
    }

    private void publishSessionUpdate(QRSession session) {
        if (messagingTemplate == null) {
            return;
        }

        QRSessionEvent event = QRSessionEvent.builder()
                .sessionId(session.getSessionId())
                .orderCode(session.getOrderCode())
                .status(session.getStatus())
                .finalPrice(session.getFinalPrice())
                .subtotal(session.getSubtotal())
                .discountAmount(session.getDiscountAmount())
                .shippingFee(session.getShippingFee())
                .qrCodeUrl(session.getQrCodeUrl())
                .expiresAt(session.getExpiresAt())
                .createdAt(session.getCreatedAt())
                .build();

        try {
            messagingTemplate.convertAndSend("/topic/qr-session", event);
        } catch (Exception e) {
            log.error("Failed to publish WebSocket message for sessionId: {}", session.getSessionId(), e);
        }
    }

    private boolean isSessionUsable(QRSession session) {
        if (!"PENDING".equals(session.getStatus())) {
            return false;
        }
        if (session.getFinalPrice() == null || session.getFinalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (session.getCartDataJson() == null || session.getCartDataJson().isBlank()) {
            return false;
        }
        return true;
    }

    private void clearSessionFinancials(QRSession session) {
        session.setQrCodeUrl(null);
        session.setCartDataJson(null);
        session.setSubtotal(BigDecimal.ZERO);
        session.setDiscountAmount(BigDecimal.ZERO);
        session.setShippingFee(BigDecimal.ZERO);
        session.setFinalPrice(BigDecimal.ZERO);
    }

    @Transactional
    public void cancelSessionsByInvoice(Integer invoiceId) {
        if (invoiceId == null) {
            return;
        }
        List<QRSession> sessions = qrSessionRepository.findAllByIdHoaDon_Id(invoiceId);
        if (sessions.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (QRSession session : sessions) {
            session.setStatus("EXPIRED");
            session.setExpiresAt(now);
            clearSessionFinancials(session);
        }
        qrSessionRepository.saveAll(sessions);
        sessions.forEach(this::publishSessionUpdate);
    }

    @Scheduled(fixedDelay = 60000)
    public void expireStaleSessions() {
        LocalDateTime now = LocalDateTime.now();
        List<QRSession> staleSessions = qrSessionRepository.findByStatusAndExpiresAtBefore("PENDING", now);
        if (staleSessions.isEmpty()) {
            return;
        }
        staleSessions.forEach(session -> {
            session.setStatus("EXPIRED");
            session.setExpiresAt(now);
            clearSessionFinancials(session);
        });
        qrSessionRepository.saveAll(staleSessions);
        staleSessions.forEach(this::publishSessionUpdate);
    }
}
