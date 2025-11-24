package org.example.be_sp.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.example.be_sp.model.request.VnpayCreatePaymentRequest;
import org.example.be_sp.model.response.VnpayCreatePaymentResponse;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VnPayService {

    private final Environment env;

    public VnpayCreatePaymentResponse createPayment(VnpayCreatePaymentRequest req, String clientIp, String origin) {
        try {
            String vnpTmnCode = get("vnp.tmnCode", "");
            String vnpHashSecret = get("vnp.hashSecret", "");
            String vnpReturnUrl = get("vnp.returnUrl", "http://localhost:8080/api/payment/vnpay/return");
            String vnpPayUrl = get("vnp.payUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");

            if (!StringUtils.hasText(vnpTmnCode) || !StringUtils.hasText(vnpHashSecret)) {
                throw new RuntimeException("VNPAY configuration is missing (tmnCode/hashSecret)");
            }

            long amount = Optional.ofNullable(req.getAmount()).orElse(0L);
            if (amount <= 0) {
                throw new RuntimeException("Invalid amount");
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

            String ip = StringUtils.hasText(clientIp) ? clientIp : "127.0.0.1";
            String createDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            cal.add(Calendar.MINUTE, 15);
            String expireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());

            // Detect frontend URL from Origin header, or use configured default
            // banHangMain runs on port 5174, admin view runs on 5173
            String clientBase = StringUtils.hasText(origin) ? origin : get("app.frontendUrl", "http://localhost:5173");
            String returnUrlWithClient = vnpReturnUrl + (vnpReturnUrl.contains("?") ? "&" : "?")
                    + "client=" + URLEncoder.encode(clientBase, StandardCharsets.UTF_8);

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
            vnp.put("vnp_ReturnUrl", returnUrlWithClient);
            vnp.put("vnp_IpAddr", ip);
            vnp.put("vnp_CreateDate", createDate);
            vnp.put("vnp_ExpireDate", expireDate);
            if (StringUtils.hasText(req.getBankCode())) {
                vnp.put("vnp_BankCode", req.getBankCode());
            }

            String query = buildQuery(vnp);
            String secureHash = hmacSHA512(vnpHashSecret, query);
            String payUrl = vnpPayUrl + "?" + query + "&vnp_SecureHash=" + secureHash;

            // VNPAY payment URL is the standard format for QR codes
            // Banking apps recognize VNPAY URLs and can process them
            // The QR code will be generated from this payment URL
            VnpayCreatePaymentResponse response = new VnpayCreatePaymentResponse();
            response.setPayUrl(payUrl);
            response.setTxnRef(txnRef);
            // qrCodeUrl is null - we'll generate QR from payUrl
            return response;
        } catch (Exception e) {
            log.error("VNPAY create payment error", e);
            throw new RuntimeException("VNPAY create error: " + e.getMessage(), e);
        }
    }

    private String get(String key, String defVal) {
        String value = env.getProperty(key);
        return (value == null || value.isBlank()) ? defVal : value;
    }

    public String getHashSecret() {
        return get("vnp.hashSecret", "");
    }

    public String getFrontendUrl() {
        return get("app.frontendUrl", "http://localhost:5173");
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
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        normalized = normalized.replaceAll("[\\u0300-\\u036f]", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "");
    }

}

