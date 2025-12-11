package org.example.be_sp.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.entity.QRSession;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.repository.QRSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment/vietqr")
@CrossOrigin(origins = "*")
@Slf4j
public class VietQRWebhookController {

    @Autowired
    private QRSessionRepository qrSessionRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(@RequestBody VietQRWebhookRequest request) {
        try {
            String addInfo = request.getAddInfo();
            String orderCode = extractOrderCode(addInfo);

            QRSession session = qrSessionRepository.findByOrderCode(orderCode)
                    .orElse(null);

            if (session != null && "PAID".equals(request.getStatus())) {
                if (!"PAID".equals(session.getStatus())) {
                    session.setStatus("PAID");
                    qrSessionRepository.save(session);

                    if (session.getIdHoaDon() != null) {
                        try {
                            HoaDon hoaDon = session.getIdHoaDon();
                            hoaDon.setTrangThaiThanhToan(true);
                            BigDecimal existingPaid = hoaDon.getSoTienDaThanhToan() != null
                                    ? hoaDon.getSoTienDaThanhToan()
                                    : BigDecimal.ZERO;
                            BigDecimal finalPrice = session.getFinalPrice() != null
                                    ? session.getFinalPrice()
                                    : BigDecimal.ZERO;
                            hoaDon.setSoTienDaThanhToan(existingPaid.add(finalPrice));
                            hoaDon.setSoTienConLai(BigDecimal.ZERO);
                            hoaDonRepository.save(hoaDon);
                        } catch (Exception e) {
                            log.error("Failed to update HoaDon payment status", e);
                        }
                    }

                    log.info("Payment confirmed for session: {} order: {}", session.getSessionId(), orderCode);
                }

                return ResponseEntity.ok(Map.of("success", true, "message", "Payment confirmed"));
            }

            return ResponseEntity.ok(Map.of("success", false, "message", "Session not found or already paid"));
        } catch (Exception e) {
            log.error("VietQR webhook error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private String extractOrderCode(String addInfo) {
        if (addInfo != null && addInfo.contains("HD")) {
            int idx = addInfo.indexOf("HD");
            return addInfo.substring(idx).trim();
        }
        return addInfo;
    }

    @Data
    public static class VietQRWebhookRequest {

        private String transactionId;
        private Long amount;
        private String addInfo;
        private String status;
        private String bankCode;
        private String transactionDate;
    }
}
