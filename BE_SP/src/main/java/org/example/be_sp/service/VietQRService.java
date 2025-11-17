package org.example.be_sp.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VietQRService {

    @Autowired
    private Environment env;

    @Autowired
    private RestTemplate restTemplate;

    public VietQRResponse createPaymentQR(VietQRRequest request) {
        String apiUrl = env.getProperty("vietqr.api.url", "https://api.vietqr.io/v2");
        String clientId = env.getProperty("vietqr.client.id");
        String apiKey = env.getProperty("vietqr.api.key");
        String accountNo = env.getProperty("vietqr.account.no");
        String accountName = env.getProperty("vietqr.account.name");
        String bankCode = env.getProperty("vietqr.bank.code");
        String template = env.getProperty("vietqr.template", "compact2");

        if (clientId == null || apiKey == null || accountNo == null) {
            throw new RuntimeException("VietQR configuration is missing. Please check application.properties");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("accountNo", accountNo);
        body.put("accountName", accountName);
        body.put("acqId", bankCode);
        body.put("amount", request.getAmount());
        body.put("addInfo", request.getAddInfo());
        body.put("format", "text");
        body.put("template", template);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<VietQRResponse> response = restTemplate.exchange(
                    apiUrl + "/generate",
                    HttpMethod.POST,
                    entity,
                    VietQRResponse.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return response.getBody();
            } else {
                throw new RuntimeException("VietQR API error: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to create VietQR: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create VietQR: " + e.getMessage(), e);
        }
    }

    @Data
    public static class VietQRRequest {
        private Long amount;
        private String addInfo;
    }

    @Data
    public static class VietQRResponse {
        private String code;
        private String desc;
        private VietQRData data;
    }

    @Data
    public static class VietQRData {
        private String qrDataURL;
        private String qrCode;
    }
}


