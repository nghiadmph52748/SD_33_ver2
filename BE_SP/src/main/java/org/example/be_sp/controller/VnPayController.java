package org.example.be_sp.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.example.be_sp.repository.OrderPaymentRepository;
import org.example.be_sp.service.VnPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payment/vnpay")
@CrossOrigin(origins = "*")
public class VnPayController {

    private final VnPayService vnPayService;
    private final OrderPaymentRepository orderRepo;

    public VnPayController(VnPayService vnPayService, OrderPaymentRepository orderRepo) {
        this.vnPayService = vnPayService;
        this.orderRepo = orderRepo;
    }

    @PostMapping("/create")
    public ResponseObject<VnpayCreatePaymentResponse> createPayment(@RequestBody VnpayCreatePaymentRequest req,
            HttpServletRequest http) {
        try {
            String ip = getClientIp(http);
            VnpayCreatePaymentResponse response = vnPayService.createPayment(req, ip);

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

        String code;
        if (receivedHash == null || !calc.equalsIgnoreCase(receivedHash)) {
            code = "97";
        } else {
            code = req.getParameter("vnp_ResponseCode");
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
        if (receivedHash == null || !calc.equalsIgnoreCase(receivedHash)) {
            resp.put("RspCode", "97");
            resp.put("Message", "Invalid signature");
            return ResponseEntity.ok(resp);
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

}
