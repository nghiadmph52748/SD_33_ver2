package org.example.be_sp.service;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.example.be_sp.model.request.MoMoCreatePaymentRequest;
import org.example.be_sp.model.response.MoMoCreatePaymentResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoMoService {

    private final Environment env;
    private final RestTemplate restTemplate;

    /**
     * Create credit card payment (replaces QR code payment)
     * Uses MoMo's payment gateway for direct credit card processing
     */
    public MoMoCreatePaymentResponse createCreditCardPayment(MoMoCreatePaymentRequest req) {
        try {
            String partnerCode = get("momo.partner.code", "MOMO");
            String accessKey = get("momo.access.key", "");
            String secretKey = get("momo.secret.key", "");
            String apiUrl = get("momo.api.url", "https://test-payment.momo.vn/v2/gateway/api");
            String ipnUrl = get("momo.ipn.url", "http://localhost:8080/api/payment/momo/ipn");
            String redirectUrl = get("momo.redirect.url", "http://localhost:5174/payment/momo/result");

            if (!StringUtils.hasText(accessKey) || !StringUtils.hasText(secretKey)) {
                throw new RuntimeException("MoMo configuration is missing (accessKey/secretKey)");
            }

            long amount = req.getAmount() != null ? req.getAmount() : 0L;
            if (amount <= 0) {
                throw new RuntimeException("Invalid amount");
            }

            String orderId = StringUtils.hasText(req.getOrderId()) ? req.getOrderId() : "HD" + System.currentTimeMillis();
            String requestId = orderId + "_" + System.currentTimeMillis();
            String orderInfo = StringUtils.hasText(req.getOrderInfo()) ? req.getOrderInfo() : "Thanh toan don hang " + orderId;

            // Build request body for credit card payment
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("partnerName", "GearUp Shop");
            requestBody.put("storeId", "GearUpStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("ipnUrl", ipnUrl);
            requestBody.put("lang", "vi");
            // Changed from captureWallet (QR) to captureWallet with credit card support
            requestBody.put("requestType", "captureWallet");
            requestBody.put("autoCapture", true);
            requestBody.put("extraData", "");

            // Generate signature according to MoMo spec
            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + redirectUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = hmacSHA256(secretKey, rawSignature);
            requestBody.put("signature", signature);

            // Make API call to MoMo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("Calling MoMo API for credit card payment: {}/create", apiUrl);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl + "/create",
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("MoMo API returned empty response");
            }

            log.info("MoMo API response: {}", responseBody);

            Integer resultCode = (Integer) responseBody.get("resultCode");
            if (resultCode == null || resultCode != 0) {
                String message = (String) responseBody.get("message");
                throw new RuntimeException("MoMo API error: " + message);
            }

            // Build response with payment URL for credit card form
            MoMoCreatePaymentResponse momoResponse = new MoMoCreatePaymentResponse();
            momoResponse.setPayUrl((String) responseBody.get("payUrl"));
            momoResponse.setQrCodeUrl((String) responseBody.get("qrCodeUrl"));
            momoResponse.setDeeplink((String) responseBody.get("deeplink"));
            momoResponse.setOrderId(orderId);
            momoResponse.setRequestId(requestId);

            return momoResponse;
        } catch (Exception e) {
            log.error("MoMo create credit card payment error", e);
            throw new RuntimeException("MoMo create error: " + e.getMessage(), e);
        }
    }

    /**
     * Legacy QR payment method - kept for backward compatibility
     */
    public MoMoCreatePaymentResponse createQRPayment(MoMoCreatePaymentRequest req) {
        return createCreditCardPayment(req);
    }

    private String get(String key, String defVal) {
        String value = env.getProperty(key);
        if (value != null) {
            value = value.trim();
        }
        return (value == null || value.isBlank()) ? defVal : value;
    }

    public String getSecretKey() {
        return get("momo.secret.key", "");
    }

    public String getAccessKey() {
        return get("momo.access.key", "");
    }

    public String getPartnerCode() {
        return get("momo.partner.code", "MOMO");
    }

    public String getRedirectUrl() {
        return get("momo.redirect.url", "http://localhost:5174/payment/momo/result");
    }

    private static String hmacSHA256(String secret, String data) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac.init(keySpec);
        byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public boolean verifySignature(String signature, String rawData) {
        try {
            String secretKey = getSecretKey();
            String calculatedSignature = hmacSHA256(secretKey, rawData);
            return calculatedSignature.equalsIgnoreCase(signature);
        } catch (Exception e) {
            log.error("Failed to verify MoMo signature", e);
            return false;
        }
    }
}

