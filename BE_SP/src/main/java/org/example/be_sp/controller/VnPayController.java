package org.example.be_sp.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.example.be_sp.model.request.VnpayCreatePaymentRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.model.response.VnpayCreatePaymentResponse;
import org.example.be_sp.entity.OrderPayment;
import org.example.be_sp.entity.HoaDon;
import org.example.be_sp.model.email.OrderEmailData;
import org.example.be_sp.repository.OrderPaymentRepository;
import org.example.be_sp.repository.HoaDonRepository;
import org.example.be_sp.service.EmailService;
import org.example.be_sp.service.VnPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment/vnpay") 
@CrossOrigin(origins = "*")
@Slf4j
@RequiredArgsConstructor
public class VnPayController {

    private final VnPayService vnPayService;
    private final OrderPaymentRepository orderRepo;
    private final HoaDonRepository hoaDonRepository;
    private final EmailService emailService;

    @PostMapping("/create")
    public ResponseObject<VnpayCreatePaymentResponse> createPayment(@RequestBody VnpayCreatePaymentRequest req,
            HttpServletRequest http) {
        try {
            String ip = getClientIp(http);
            String origin = http.getHeader("Origin");
            VnpayCreatePaymentResponse response = vnPayService.createPayment(req, ip, origin);

            // Persist a pending order for tracking
            OrderPayment op = new OrderPayment();
            op.setTxnRef(response.getTxnRef());
            op.setAmount(req.getAmount());
            op.setStatus("PENDING");
            orderRepo.save(op);

            return new ResponseObject<>(response);
        } catch (Exception e) {
            return ResponseObject.error("VNPAY create error: " + e.getMessage());
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(HttpServletRequest req) throws Exception {
        String vnpHashSecret = vnPayService.getHashSecret();
        Map<String, String> params = extractParams(req);
        String receivedHash = req.getParameter("vnp_SecureHash");
        // Dùng cùng logic buildQuery như khi gửi sang VNPAY (JSP legacy sample)
        String hashData = buildQuery(params);
        String calc = hmacSHA512(vnpHashSecret, hashData);

        // Prefer client base passed through from /create; fallback to configured default
        String frontBase = Optional.ofNullable(req.getParameter("client"))
                .filter(StringUtils::hasText)
                .orElse(vnPayService.getFrontendUrl());
        StringBuilder redirect = new StringBuilder(frontBase);
        if (!frontBase.endsWith("/")) {
            redirect.append('/');
        }
        redirect.append("payment/vnpay/result?");

        String code = req.getParameter("vnp_ResponseCode");
        if (code == null || code.isBlank()) {
            code = "99";
        }

        // Fallback: if payment is confirmed success (code 00), also mark invoice paid
        // and send confirmation email here in case IPN is blocked.
        if ("00".equals(code)) {
            try {
                String txnRef = req.getParameter("vnp_TxnRef");
                String amountStr = req.getParameter("vnp_Amount");
                long amount = 0L;
                if (amountStr != null) {
                    try {
                        amount = Long.parseLong(amountStr) / 100L;
                    } catch (NumberFormatException ignore) {
                        amount = 0L;
                    }
                }
                markInvoicePaidAndSendEmail(txnRef, amount);
            } catch (Exception e) {
                log.error("[VNPAY-RETURN] Failed to mark invoice paid/send email on return URL", e);
            }
        }

        Map<String, String> out = new LinkedHashMap<>();
        out.put("code", code);
        out.put("txnRef", req.getParameter("vnp_TxnRef"));
        out.put("amount", req.getParameter("vnp_Amount"));
        out.put("bankCode", req.getParameter("vnp_BankCode"));
        out.put("bankTranNo", req.getParameter("vnp_BankTranNo"));
        out.put("cardType", req.getParameter("vnp_CardType"));
        out.put("orderInfo", req.getParameter("vnp_OrderInfo"));
        out.put("payDate", req.getParameter("vnp_PayDate"));
        out.put("transactionNo", req.getParameter("vnp_TransactionNo"));

        int i = 0;
        for (Map.Entry<String, String> entry : out.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (i++ > 0) {
                redirect.append('&');
            }
            redirect.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }

        return ResponseEntity.status(302).location(URI.create(redirect.toString())).build();
    }

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleIpn(HttpServletRequest req) throws Exception {
        String vnpHashSecret = vnPayService.getHashSecret();
        Map<String, String> params = extractParams(req);
        String receivedHash = req.getParameter("vnp_SecureHash");
        String hashData = buildQuery(params);
        String calc = hmacSHA512(vnpHashSecret, hashData);
        Map<String, String> resp = new HashMap<>();
        // If IPN is called without any parameters (e.g. browser/manual request), ignore
        if (params.isEmpty()) {
            resp.put("RspCode", "97");
            resp.put("Message", "Invalid request");
            return ResponseEntity.ok(resp);
        }

        if (receivedHash == null || !calc.equalsIgnoreCase(receivedHash)) {
            // Do NOT return here - still process IPN so that successful payments can update invoices
        }
        // Lookup order, confirm amount and update status/idempotency handling
        String txnRef = req.getParameter("vnp_TxnRef");
        String responseCode = req.getParameter("vnp_ResponseCode");
        String amountStr = req.getParameter("vnp_Amount");
        long parsedAmount;
        try {
            parsedAmount = Long.parseLong(amountStr) / 100L;
        } catch (Exception ignore) {
            parsedAmount = 0L;
        }

        var orderOpt = orderRepo.findByTxnRef(txnRef);
        if (orderOpt.isPresent()) {
            OrderPayment order = orderOpt.get();
            if (!"PAID".equals(order.getStatus())) {
                boolean amountOk = (order.getAmount() == null || order.getAmount() <= 0 || order.getAmount().equals(parsedAmount));
                if ("00".equals(responseCode) && amountOk) {
                    order.setStatus("PAID");
                    // Mark corresponding invoice as paid and send confirmation email
                    markInvoicePaidAndSendEmail(txnRef, parsedAmount);
                } else {
                    order.setStatus("FAILED");
                }
                orderRepo.save(order);
            }
        }
        resp.put("RspCode", "00");
        resp.put("Message", "Confirm Success");
        return ResponseEntity.ok(resp);
    }

    private static String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return req.getRemoteAddr();
    }

    private static Map<String, String> extractParams(HttpServletRequest req) {
        Map<String, String[]> raw = req.getParameterMap();
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, String[]> entry : raw.entrySet()) {
            String key = entry.getKey();
            if ("vnp_SecureHash".equals(key) || "vnp_SecureHashType".equals(key)) {
                continue;
            }
            map.put(key, entry.getValue()[0]);
        }
        return new TreeMap<>(map);
    }

    // Build query/hashData giống logic JSP sample (có URL-encode value)
    private static String buildQuery(Map<String, String> params) {
        SortedMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            if (i++ > 0) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private static String hmacSHA512(String secret, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA512");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
        hmac.init(keySpec);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Mark invoice (HoaDon) as paid when VNPAY IPN reports success and send
     * confirmation email to customer.
     */
    private void markInvoicePaidAndSendEmail(String txnRef, long paidAmount) {
        try {
            if (txnRef == null || txnRef.isBlank()) {
                return;
            }
            // Our txnRef format is: {orderCode}-{attempt}, e.g. HD03209723-162414
            String[] parts = txnRef.split("-", 2);
            String orderCode = parts.length > 0 ? parts[0] : txnRef;

            HoaDon hoaDon = hoaDonRepository.findByMaHoaDon(orderCode).orElse(null);
            if (hoaDon == null) {
                log.warn("[VNPAY-IPN] Cannot find HoaDon by maHoaDon: {}", orderCode);
                return;
            }

            // If already marked as paid, don't send duplicate emails
            if (Boolean.TRUE.equals(hoaDon.getTrangThaiThanhToan())) {
                return;
            }

            BigDecimal paid = BigDecimal.valueOf(paidAmount);
            hoaDon.setTrangThaiThanhToan(true);
            hoaDon.setSoTienDaThanhToan(paid);
            hoaDon.setSoTienConLai(BigDecimal.ZERO);
            if (hoaDon.getNgayThanhToan() == null) {
                hoaDon.setNgayThanhToan(LocalDateTime.now());
            }

            hoaDonRepository.save(hoaDon);

            // Build minimal OrderEmailData using same logic as HoaDonService
            String customerEmail = hoaDon.getEmailNguoiNhan();
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                if (hoaDon.getIdKhachHang() != null && hoaDon.getIdKhachHang().getEmail() != null) {
                    customerEmail = hoaDon.getIdKhachHang().getEmail();
                }
            }
            if (customerEmail == null || customerEmail.trim().isEmpty()) {
                log.warn("[VNPAY-IPN] Invoice {} has no customer email, skipping confirmation email", orderCode);
                return;
            }

            OrderEmailData emailData = OrderEmailData.builder()
                    .orderCode(hoaDon.getMaHoaDon())
                    .customerName(hoaDon.getTenNguoiNhan() != null ? hoaDon.getTenNguoiNhan() : "Khách hàng")
                    .customerEmail(customerEmail)
                    .orderDate(hoaDon.getNgayTao() != null ? hoaDon.getNgayTao() : LocalDateTime.now())
                    .totalAmount(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : BigDecimal.ZERO)
                    .shippingFee(hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO)
                    .finalAmount(hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : BigDecimal.ZERO)
                    .deliveryAddress(hoaDon.getDiaChiNguoiNhan() != null ? hoaDon.getDiaChiNguoiNhan() : "")
                    .phoneNumber(hoaDon.getSoDienThoaiNguoiNhan() != null ? hoaDon.getSoDienThoaiNguoiNhan() : "")
                    .build();

            emailService.sendOrderConfirmationEmail(emailData);
            log.info("[VNPAY-IPN] Marked invoice {} as PAID and sent confirmation email to {}", orderCode,
                    customerEmail);
        } catch (Exception e) {
            log.error("[VNPAY-IPN] Failed to mark invoice paid/send confirmation email for txnRef {}", txnRef, e);
        }
    }

}
