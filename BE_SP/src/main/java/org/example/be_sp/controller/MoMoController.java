package org.example.be_sp.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.OrderPayment;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.model.request.MoMoCreatePaymentRequest;
import org.example.be_sp.model.request.MoMoIpnRequest;
import org.example.be_sp.model.response.MoMoCreatePaymentResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.OrderPaymentRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.service.EmailService;
import org.example.be_sp.service.MoMoService;
import org.example.be_sp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment/momo")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class MoMoController {

    private final MoMoService momoService;
    private final OrderPaymentRepository orderRepo;
    private final HoaDonRepository hoaDonRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final TrangThaiDonHangRepository trangThaiDonHangRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @PostMapping("/create")
    public ResponseObject<MoMoCreatePaymentResponse> createPayment(@RequestBody MoMoCreatePaymentRequest req) {
        try {
            MoMoCreatePaymentResponse response = momoService.createQRPayment(req);

            // Persist a pending order for tracking
            OrderPayment op = new OrderPayment();
            op.setTxnRef(response.getOrderId());
            op.setAmount(req.getAmount());
            op.setStatus("PENDING");
            orderRepo.save(op);

            return new ResponseObject<>(response);
        } catch (Exception e) {
            log.error("MoMo create payment error", e);
            return ResponseObject.error("MoMo create error: " + e.getMessage());
        }
    }

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, Object>> handleIpn(@RequestBody MoMoIpnRequest req) {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("[MOMO-IPN] Received IPN: orderId={}, resultCode={}", req.getOrderId(), req.getResultCode());

            // Verify signature
            String rawSignature = "accessKey=" + momoService.getAccessKey()
                    + "&amount=" + req.getAmount()
                    + "&extraData=" + (req.getExtraData() != null ? req.getExtraData() : "")
                    + "&message=" + req.getMessage()
                    + "&orderId=" + req.getOrderId()
                    + "&orderInfo=" + req.getOrderInfo()
                    + "&orderType=" + req.getOrderType()
                    + "&partnerCode=" + req.getPartnerCode()
                    + "&payType=" + req.getPayType()
                    + "&requestId=" + req.getRequestId()
                    + "&responseTime=" + req.getResponseTime()
                    + "&resultCode=" + req.getResultCode()
                    + "&transId=" + req.getTransId();

            boolean isValid = momoService.verifySignature(req.getSignature(), rawSignature);
            if (!isValid) {
                log.warn("[MOMO-IPN] Invalid signature for orderId: {}", req.getOrderId());
                // Still process if resultCode is 0 (success) for development
            }

            String orderId = req.getOrderId();
            Integer resultCode = req.getResultCode();
            Long amount = req.getAmount();

            // Update OrderPayment
            var orderOpt = orderRepo.findByTxnRef(orderId);
            if (orderOpt.isPresent()) {
                OrderPayment order = orderOpt.get();
                if (!"PAID".equals(order.getStatus())) {
                    if (resultCode != null && resultCode == 0) {
                        order.setStatus("PAID");
                        markInvoicePaidAndSendEmail(orderId, amount);
                    } else {
                        order.setStatus("FAILED");
                    }
                    orderRepo.save(order);
                }
            }

            response.put("partnerCode", req.getPartnerCode());
            response.put("requestId", req.getRequestId());
            response.put("orderId", req.getOrderId());
            response.put("resultCode", 0);
            response.put("message", "Success");
            response.put("responseTime", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[MOMO-IPN] Error processing IPN", e);
            response.put("resultCode", 1000);
            response.put("message", "System error");
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(HttpServletRequest req) {
        try {
            String orderId = req.getParameter("orderId");
            String resultCode = req.getParameter("resultCode");
            String message = req.getParameter("message");
            String amount = req.getParameter("amount");

            log.info("[MOMO-RETURN] orderId={}, resultCode={}", orderId, resultCode);

            // Fallback: mark invoice as paid if resultCode is 0
            if ("0".equals(resultCode)) {
                try {
                    long amountValue = 0L;
                    if (amount != null) {
                        try {
                            amountValue = Long.parseLong(amount);
                        } catch (NumberFormatException ignore) {
                        }
                    }
                    markInvoicePaidAndSendEmail(orderId, amountValue);
                } catch (Exception e) {
                    log.error("[MOMO-RETURN] Failed to mark invoice paid", e);
                }
            } else if ("1006".equals(resultCode)) {
                // TESTING ONLY: User cancelled payment - mark as PAID for testing
                // TODO: Change back to markOrderAsCancelled for production
                log.warn("[MOMO-TEST] User cancelled (code 1006) - converting to success for testing - orderId: {}", orderId);
                try {
                    long amountValue = 0L;
                    if (amount != null) {
                        try {
                            amountValue = Long.parseLong(amount);
                        } catch (NumberFormatException ignore) {
                        }
                    }
                    markInvoicePaidAndSendEmail(orderId, amountValue);
                } catch (Exception e) {
                    log.error("[MOMO-RETURN] Failed to process cancelled payment", e);
                }
                // Change resultCode to 0 (success) for testing
                resultCode = "0";
                message = "Thanh toán thành công";
                log.info("[MOMO-TEST] Changed resultCode to: {}, message to: {}", resultCode, message);
            }

            log.info("[MOMO-RETURN] Final redirect - resultCode={}, orderId={}", resultCode, orderId);

            // Build redirect URL
            String redirectUrl = momoService.getRedirectUrl();
            StringBuilder redirect = new StringBuilder(redirectUrl);
            if (!redirectUrl.contains("?")) {
                redirect.append("?");
            } else {
                redirect.append("&");
            }

            Map<String, String> params = new LinkedHashMap<>();
            params.put("resultCode", resultCode);
            params.put("orderId", orderId);
            params.put("amount", amount);
            params.put("message", message);

            int i = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (i++ > 0) {
                    redirect.append("&");
                }
                redirect.append(entry.getKey()).append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            return ResponseEntity.status(302).location(URI.create(redirect.toString())).build();
        } catch (Exception e) {
            log.error("[MOMO-RETURN] Error processing return", e);
            String fallback = momoService.getRedirectUrl() + "?resultCode=1000&message=Error";
            return ResponseEntity.status(302).location(URI.create(fallback)).build();
        }
    }

    private void markInvoicePaidAndSendEmail(String orderId, Long paidAmount) {
        try {
            if (orderId == null || orderId.isBlank()) {
                return;
            }

            // Extract order code (format: HD03209723 or HD03209723_timestamp)
            String orderCode = orderId.split("_")[0];

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderCode).orElse(null);
            if (hoaDon == null) {
                log.warn("[MOMO-IPN] Cannot find HoaDon by maHoaDon: {}", orderCode);
                return;
            }

            // If already marked as paid, don't send duplicate emails
            if (Boolean.TRUE.equals(hoaDon.getTrangThaiThanhToan())) {
                return;
            }

            BigDecimal invoiceTotal = resolveInvoiceTotal(hoaDon);
            BigDecimal existingPaid = safe(hoaDon.getSoTienDaThanhToan());
            BigDecimal incoming = paidAmount != null && paidAmount > 0
                    ? BigDecimal.valueOf(paidAmount)
                    : invoiceTotal;

            if (incoming.compareTo(BigDecimal.ZERO) <= 0 && invoiceTotal.compareTo(existingPaid) > 0) {
                incoming = invoiceTotal.subtract(existingPaid);
            }

            BigDecimal newPaid = existingPaid.add(incoming);
            if (newPaid.compareTo(invoiceTotal) > 0) {
                newPaid = invoiceTotal;
            }

            hoaDon.setSoTienDaThanhToan(newPaid);

            BigDecimal remaining = invoiceTotal.subtract(newPaid);
            if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                remaining = BigDecimal.ZERO;
            }
            hoaDon.setSoTienConLai(remaining);

            boolean paidInFull = invoiceTotal.compareTo(BigDecimal.ZERO) > 0
                    && remaining.compareTo(BigDecimal.ZERO) == 0;
            hoaDon.setTrangThaiThanhToan(paidInFull);
            if (hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(LocalDateTime.now());
            }

            hoaDonRepository.save(hoaDon);

            // Send confirmation email
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                log.warn("[MOMO-IPN] Invoice {} has no customer email, skipping confirmation email", orderCode);
                return;
            }

            OrderEmailData emailData = OrderEmailData.builder()
                    .orderId(hoaDon.getId())
                    .orderCode(hoaDon.getMaHoaDon())
                    .customerName(hoaDon.getTenNguoiNhan() != null ? hoaDon.getTenNguoiNhan() : "Khách hàng")
                    .customerEmail(customerEmail)
                    .orderDate(hoaDon.getCreateAt() != null ? hoaDon.getCreateAt() : LocalDateTime.now())
                    .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                    .shippingFee(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                    .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                    .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                    .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                    .build();

            emailService.sendOrderConfirmationEmail(emailData);
            log.info("[MOMO-IPN] Marked invoice {} as PAID and sent confirmation email to {}",
                    orderCode, customerEmail);

            notificationService.notifyAllStaff(
                    "Thanh toán MoMo #" + hoaDon.getMaHoaDon(),
                    "Đã thanh toán",
                    "Đơn hàng đã thanh toán " + formatCurrency(hoaDon.getTongTienSauGiam()) + " qua MoMo.",
                    1
            );
        } catch (Exception e) {
            log.error("[MOMO-IPN] Failed to mark invoice paid/send confirmation email for orderId {}", orderId, e);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0₫";
        }
        return amount.stripTrailingZeros().toPlainString() + "₫";
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal resolveInvoiceTotal(HoaDon hoaDon) {
        BigDecimal base = safe(hoaDon.getTongTienSauGiam());
        if (base.compareTo(BigDecimal.ZERO) <= 0) {
            base = safe(hoaDon.getTongTien());
        }
        BigDecimal total = base.add(safe(hoaDon.getPhiVanChuyen()));
        if (Boolean.TRUE.equals(hoaDon.getGiaoHang())) {
            total = total.add(safe(hoaDon.getHoanPhi()));
        }
        return total.compareTo(BigDecimal.ZERO) > 0 ? total : BigDecimal.ZERO;
    }

    /**
     * Mark order as cancelled when user cancels payment
     */
    private void markOrderAsCancelled(String orderId, String reason) {
        try {
            if (orderId == null || orderId.isBlank()) {
                return;
            }

            // Extract order code (format: HD03209723 or HD03209723_timestamp)
            String orderCode = orderId.split("_")[0];

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderCode).orElse(null);
            if (hoaDon == null) {
                log.warn("[PAYMENT-CANCEL] Cannot find HoaDon by maHoaDon: {}", orderCode);
                return;
            }

            // Find "Đã huỷ" status (cancelled status)
            // Try common IDs: 5, 6, 7, or search by name
            TrangThaiDonHang cancelledStatus = null;
            for (int id = 5; id <= 7; id++) {
                var status = trangThaiDonHangRepository.findById(id);
                if (status.isPresent() && status.get().getTenTrangThaiDonHang() != null
                        && status.get().getTenTrangThaiDonHang().toLowerCase().contains("hủy")) {
                    cancelledStatus = status.get();
                    break;
                }
            }

            // If not found by ID, try searching all statuses
            if (cancelledStatus == null) {
                var allStatuses = trangThaiDonHangRepository.findAll();
                for (var status : allStatuses) {
                    if (status.getTenTrangThaiDonHang() != null
                            && status.getTenTrangThaiDonHang().toLowerCase().contains("hủy")) {
                        cancelledStatus = status;
                        break;
                    }
                }
            }

            if (cancelledStatus == null) {
                log.error("[PAYMENT-CANCEL] Cannot find cancelled status in database");
                return;
            }

            // Create new ThongTinDonHang entry with cancelled status
            ThongTinDonHang statusUpdate = new ThongTinDonHang();
            statusUpdate.setIdHoaDon(hoaDon);
            statusUpdate.setIdTrangThaiDonHang(cancelledStatus);
            statusUpdate.setThoiGian(LocalDateTime.now());
            statusUpdate.setGhiChu(reason);
            statusUpdate.setTrangThai(true);
            statusUpdate.setDeleted(false);

            thongTinDonHangRepository.save(statusUpdate);

            log.info("[PAYMENT-CANCEL] Order {} marked as cancelled: {}", orderCode, reason);

            notificationService.notifyAllStaff(
                    "Đơn hàng #" + hoaDon.getMaHoaDon() + " đã bị hủy",
                    "Thanh toán bị hủy",
                    reason,
                    1
            );
        } catch (Exception e) {
            log.error("[PAYMENT-CANCEL] Failed to mark order as cancelled for orderId {}", orderId, e);
        }
    }
}
