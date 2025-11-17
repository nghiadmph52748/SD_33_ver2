package org.example.be_sp.controller;

import org.example.be_sp.model.request.CreateQRSessionRequest;
import org.example.be_sp.model.response.QRSessionResponse;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.service.QRSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/pos/qr-session")
@CrossOrigin(origins = "*")
@Slf4j
public class QRSessionController {

    @Autowired
    private QRSessionService qrSessionService;

    @PostMapping
    public ResponseEntity<ResponseObject<QRSessionResponse>> createSession(
            @RequestBody CreateQRSessionRequest request) {
        try {
            QRSessionResponse session = qrSessionService.createSession(request);
            return ResponseEntity.ok(new ResponseObject<>(session, "Tạo QR session thành công"));
        } catch (Exception e) {
            log.error("Failed to create QR session", e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.<QRSessionResponse>error("Không thể tạo QR session: " + e.getMessage()));
        }
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ResponseObject<QRSessionResponse>> getSession(
            @PathVariable String sessionId) {
        try {
            QRSessionResponse session = qrSessionService.getSession(sessionId);
            return ResponseEntity.ok(new ResponseObject<>(session, "Lấy thông tin session thành công"));
        } catch (Exception e) {
            log.error("Failed to get QR session", e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.<QRSessionResponse>error("Không thể lấy thông tin session: " + e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<ResponseObject<QRSessionResponse>> getLatestSession() {
        try {
            QRSessionResponse session = qrSessionService.getLatestActiveSession();
            return ResponseEntity.ok(new ResponseObject<>(session, "Lấy session mới nhất thành công"));
        } catch (RuntimeException e) {
            // Không có session PENDING nào là tình huống bình thường -> log ở mức INFO, không cần stack trace
            if (e.getMessage() != null && e.getMessage().contains("Không có phiên VietQR đang hoạt động")) {
                log.info("No active QR session found when calling /latest");
                return ResponseEntity.ok(new ResponseObject<>(null, "Không có phiên VietQR đang hoạt động"));
            }

            log.error("Failed to get latest QR session", e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.<QRSessionResponse>error("Không thể lấy session mới nhất: " + e.getMessage()));
        }
    }

    @PutMapping("/{sessionId}")
    public ResponseEntity<ResponseObject<QRSessionResponse>> updateSession(
            @PathVariable String sessionId,
            @RequestBody CreateQRSessionRequest request) {
        try {
            QRSessionResponse session = qrSessionService.updateSession(sessionId, request);
            return ResponseEntity.ok(new ResponseObject<>(session, "Cập nhật QR session thành công"));
        } catch (Exception e) {
            log.error("Failed to update QR session {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.<QRSessionResponse>error("Không thể cập nhật QR session: " + e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/payment")
    public ResponseEntity<ResponseObject<?>> confirmPayment(@PathVariable String sessionId) {
        try {
            qrSessionService.updateStatus(sessionId, "PAID");
            return ResponseEntity.ok(new ResponseObject<>(true, "Xác nhận thanh toán thành công"));
        } catch (Exception e) {
            log.error("Failed to confirm payment", e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.error("Không thể xác nhận thanh toán: " + e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/cancel")
    public ResponseEntity<ResponseObject<?>> cancelSession(@PathVariable String sessionId) {
        try {
            qrSessionService.updateStatus(sessionId, "EXPIRED");
            return ResponseEntity.ok(new ResponseObject<>(true, "Đã huỷ/đóng phiên thanh toán"));
        } catch (Exception e) {
            log.error("Failed to cancel QR session {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.error("Không thể huỷ session: " + e.getMessage()));
        }
    }

    @PostMapping("/{sessionId}/qr")
    public ResponseEntity<ResponseObject<QRSessionResponse>> generateQr(
            @PathVariable String sessionId) {
        try {
            QRSessionResponse session = qrSessionService.generateQrForSession(sessionId);
            return ResponseEntity.ok(new ResponseObject<>(session, "Tạo mã QR thành công"));
        } catch (Exception e) {
            log.error("Failed to generate QR for session {}", sessionId, e);
            return ResponseEntity.badRequest()
                    .body(ResponseObject.<QRSessionResponse>error("Không thể tạo mã QR: " + e.getMessage()));
        }
    }
}


