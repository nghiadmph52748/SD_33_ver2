package org.example.be_sp.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.ThongTinDonHang;
import org.example.be_sp.entity.TrangThaiDonHang;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.OrderPaymentRepository;
import org.example.be_sp.repository.ThongTinDonHangRepository;
import org.example.be_sp.repository.TrangThaiDonHangRepository;
import org.example.be_sp.service.EmailService;
import org.example.be_sp.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/test/payment")
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class TestPaymentController {

    private final HoaDonRepository hoaDonRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ThongTinDonHangRepository thongTinDonHangRepository;
    private final TrangThaiDonHangRepository trangThaiDonHangRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * TEST ONLY: Manually mark an order as paid via browser (redirects to success page)
     * DELETE THIS ENDPOINT IN PRODUCTION
     * 
     * Usage: GET http://localhost:8080/api/test/payment/complete-order/HD19179768
     */
    @GetMapping("/complete-order/{orderCode}")
    public ResponseEntity<Void> completeOrderViaBrowser(
            @PathVariable String orderCode,
            @RequestParam(defaultValue = "momo") String provider) {
        try {
            log.warn("[TEST-PAYMENT] Browser access - marking order {} as PAID", orderCode);
            
            // Mark order as paid
            markOrderAsPaid(orderCode);
            
            // Redirect to success page based on provider
            String frontendUrl = "http://localhost:5174";
            String resultPath = provider.equals("vnpay") ? "/payment/vnpay/result" : "/payment/momo/result";
            
            StringBuilder redirectUrl = new StringBuilder(frontendUrl);
            redirectUrl.append(resultPath).append("?");
            
            if (provider.equals("vnpay")) {
                redirectUrl.append("code=00");
                redirectUrl.append("&txnRef=").append(URLEncoder.encode(orderCode, StandardCharsets.UTF_8));
            } else {
                redirectUrl.append("resultCode=0");
                redirectUrl.append("&orderId=").append(URLEncoder.encode(orderCode, StandardCharsets.UTF_8));
            }
            
            redirectUrl.append("&amount=999999");
            redirectUrl.append("&message=").append(URLEncoder.encode("TEST: Thanh toán thành công", StandardCharsets.UTF_8));
            
            return ResponseEntity.status(302).location(URI.create(redirectUrl.toString())).build();
            
        } catch (Exception e) {
            log.error("[TEST-PAYMENT] Error completing order", e);
            String errorUrl = "http://localhost:5174/?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return ResponseEntity.status(302).location(URI.create(errorUrl)).build();
        }
    }

    /**
     * TEST ONLY: API endpoint to mark order as paid (returns JSON)
     * DELETE THIS ENDPOINT IN PRODUCTION
     */
    @PostMapping("/mark-paid/{orderCode}")
    public ResponseEntity<Map<String, Object>> markOrderAsPaidForTesting(@PathVariable String orderCode) {
        try {
            BigDecimal total = markOrderAsPaid(orderCode);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order marked as paid (TEST ONLY)",
                    "orderCode", orderCode,
                    "amount", total.toString()
            ));

        } catch (Exception e) {
            log.error("[TEST-PAYMENT] Error marking order as paid", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Internal method to mark order as paid
     */
    private BigDecimal markOrderAsPaid(String orderCode) {
        try {
            log.warn("[TEST-PAYMENT] Manually marking order {} as PAID - THIS IS FOR TESTING ONLY", orderCode);

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderCode));

            // Mark invoice as paid
            BigDecimal invoiceTotal = resolveInvoiceTotal(hoaDon);
            hoaDon.setSoTienDaThanhToan(invoiceTotal);
            hoaDon.setSoTienConLai(BigDecimal.ZERO);
            hoaDon.setTrangThaiThanhToan(true);
            hoaDon.setNgayThanhToan(LocalDateTime.now());
            hoaDonRepository.save(hoaDon);

            // Update OrderPayment if exists
            orderPaymentRepository.findByTxnRef(orderCode).ifPresent(payment -> {
                payment.setStatus("PAID");
                orderPaymentRepository.save(payment);
            });

            // Find "Đã xác nhận" or "Đang giao hàng" status
            TrangThaiDonHang confirmedStatus = null;
            for (int id = 2; id <= 4; id++) {
                var status = trangThaiDonHangRepository.findById(id);
                if (status.isPresent() && status.get().getTenTrangThaiDonHang() != null 
                        && (status.get().getTenTrangThaiDonHang().toLowerCase().contains("xác nhận")
                            || status.get().getTenTrangThaiDonHang().toLowerCase().contains("giao"))) {
                    confirmedStatus = status.get();
                    break;
                }
            }

            if (confirmedStatus != null) {
                ThongTinDonHang statusUpdate = new ThongTinDonHang();
                statusUpdate.setIdHoaDon(hoaDon);
                statusUpdate.setIdTrangThaiDonHang(confirmedStatus);
                statusUpdate.setThoiGian(LocalDateTime.now());
                statusUpdate.setGhiChu("TEST: Thanh toán thủ công qua test endpoint");
                statusUpdate.setTrangThai(true);
                statusUpdate.setDeleted(false);
                thongTinDonHangRepository.save(statusUpdate);
            }

            // Send confirmation email
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }

            if (customerEmail != null && !customerEmail.trim().isEmpty()) {
                OrderEmailData emailData = OrderEmailData.builder()
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
            }

            notificationService.notifyAllStaff(
                    "TEST: Đơn hàng #" + hoaDon.getMaHoaDon(),
                    "Đã thanh toán (TEST)",
                    "Đơn hàng được đánh dấu đã thanh toán qua test endpoint",
                    1
            );

            return invoiceTotal;
        } catch (Exception e) {
            log.error("[TEST-PAYMENT] Error in markOrderAsPaid", e);
            throw new RuntimeException(e.getMessage(), e);
        }
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
}

