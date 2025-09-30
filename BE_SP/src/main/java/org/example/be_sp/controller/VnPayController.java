package org.example.be_sp.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.example.be_sp.model.request.VnpayCreatePaymentRequest;
import org.example.be_sp.model.response.ResponseObject;
import org.example.be_sp.model.response.VnpayCreatePaymentResponse;
import org.springframework.core.env.Environment;
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

    private final Environment env;

    public VnPayController(Environment env) {
        this.env = env;
    }

    @PostMapping("/create")
    public ResponseObject<VnpayCreatePaymentResponse> createPayment(@RequestBody VnpayCreatePaymentRequest req,
            HttpServletRequest http) {
        try {
            String vnpTmnCode = get("vnp.tmnCode", "");
            String vnpHashSecret = get("vnp.hashSecret", "");
            String vnpReturnUrl = get("vnp.returnUrl", "http://localhost:8080/api/payment/vnpay/return");
            String vnpPayUrl = get("vnp.payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

            if (!StringUtils.hasText(vnpTmnCode) || !StringUtils.hasText(vnpHashSecret)) {
                return ResponseObject.error("VNPAY configuration is missing (tmnCode/hashSecret)");
            }

            long amount = Optional.ofNullable(req.getAmount()).orElse(0L);
            if (amount <= 0) {
                return ResponseObject.error("Invalid amount");
            }

            String locale = StringUtils.hasText(req.getLocale()) ? req.getLocale() : "vn";
            String orderInfoRaw = StringUtils.hasText(req.getOrderInfo()) ? req.getOrderInfo() : "Thanh toan don hang";
            String orderInfo = normalizeAscii(orderInfoRaw);

            String baseId = StringUtils.hasText(req.getOrderId()) ? req.getOrderId() : "HD";
            String attempt = String.valueOf(System.currentTimeMillis() % 1_000_000L);
            String txnRef = (baseId + "-" + attempt).replaceAll("[^A-Za-z0-9_-]", "");
            if (txnRef.length() > 32) {
                txnRef = txnRef.substring(0, 32);
            }

            String ip = getClientIp(http);
            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            cal.add(Calendar.MINUTE, 15);
            String expireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());

            Map<String, String> vnp = new HashMap<>();
            vnp.put("vnp_Version", "2.1.0");
            vnp.put("vnp_Command", "pay");
            vnp.put("vnp_TmnCode", vnpTmnCode);
            vnp.put("vnp_Amount", String.valueOf(amount * 100));
            vnp.put("vnp_CurrCode", "VND");
            vnp.put("vnp_TxnRef", txnRef);
            vnp.put("vnp_OrderInfo", orderInfo);
            vnp.put("vnp_OrderType", "other");
            vnp.put("vnp_Locale", locale);
            vnp.put("vnp_ReturnUrl", vnpReturnUrl);
            vnp.put("vnp_IpAddr", ip);
            vnp.put("vnp_CreateDate", createDate);
            vnp.put("vnp_ExpireDate", expireDate);
            if (StringUtils.hasText(req.getBankCode())) {
                vnp.put("vnp_BankCode", req.getBankCode());
            }

            String query = buildQuery(vnp);
            String secureHash = hmacSHA512(vnpHashSecret, query);
            String payUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

            return new ResponseObject<>(new VnpayCreatePaymentResponse(payUrl, txnRef));
        } catch (Exception e) {
            return ResponseObject.error("VNPAY create error: " + e.getMessage());
        }
    }

    @GetMapping("/return")
    public ResponseEntity<Void> handleReturn(HttpServletRequest req) throws Exception {
        String vnpHashSecret = get("vnp.hashSecret", "");
        Map<String, String> params = extractParams(req);
        String receivedHash = req.getParameter("vnp_SecureHash");
        String hashData = buildQuery(params);
        String calc = hmacSHA512(vnpHashSecret, hashData);

        String frontBase = get("app.frontendUrl", "http://localhost:5173");
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
        String vnpHashSecret = get("vnp.hashSecret", "");
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
        // TODO: lookup order, confirm amount and update status/idempotency handling
        resp.put("RspCode", "00");
        resp.put("Message", "Confirm Success");
        return ResponseEntity.ok(resp);
    }

    private String get(String key, String defVal) {
        String value = env.getProperty(key);
        return (value == null || value.isBlank()) ? defVal : value;
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

    private static String normalizeAscii(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\u0300-\\u036f]", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "");
    }
}
