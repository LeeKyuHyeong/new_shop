package com.kh.shop.controller.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.shop.config.PaymentConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {

    @Autowired
    private PaymentConfig paymentConfig;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 결제 설정 정보 (프론트엔드용)
    @GetMapping("/config")
    public Map<String, Object> getPaymentConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("impCode", paymentConfig.getImpCode());
        config.put("testMode", paymentConfig.isTestMode());
        return config;
    }

    // 결제 검증 API
    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        String impUid = (String) request.get("imp_uid");
        String merchantUid = (String) request.get("merchant_uid");
        Integer expectedAmount = (Integer) request.get("amount");

        if (impUid == null || merchantUid == null || expectedAmount == null) {
            result.put("success", false);
            result.put("message", "필수 파라미터가 누락되었습니다.");
            return result;
        }

        try {
            // 1. 액세스 토큰 발급
            String accessToken = getAccessToken();

            if (accessToken == null) {
                result.put("success", false);
                result.put("message", "포트원 인증에 실패했습니다.");
                return result;
            }

            // 2. 결제 정보 조회
            String paymentUrl = "https://api.iamport.kr/payments/" + impUid;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(paymentUrl, HttpMethod.GET, entity, String.class);

            JsonNode paymentData = objectMapper.readTree(response.getBody());
            JsonNode paymentResponse = paymentData.get("response");

            if (paymentResponse == null) {
                result.put("success", false);
                result.put("message", "결제 정보를 찾을 수 없습니다.");
                return result;
            }

            // 3. 결제 금액 검증
            int paidAmount = paymentResponse.get("amount").asInt();
            String status = paymentResponse.get("status").asText();

            if (paidAmount != expectedAmount) {
                result.put("success", false);
                result.put("message", "결제 금액이 일치하지 않습니다.");
                return result;
            }

            if (!"paid".equals(status)) {
                result.put("success", false);
                result.put("message", "결제가 완료되지 않았습니다. 상태: " + status);
                return result;
            }

            // 검증 성공
            result.put("success", true);
            result.put("message", "결제가 검증되었습니다.");
            result.put("impUid", impUid);
            result.put("merchantUid", merchantUid);
            result.put("amount", paidAmount);
            result.put("status", status);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "결제 검증 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    // 포트원 액세스 토큰 발급
    private String getAccessToken() {
        try {
            String tokenUrl = "https://api.iamport.kr/users/getToken";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> body = new HashMap<>();
            body.put("imp_key", paymentConfig.getApiKey());
            body.put("imp_secret", paymentConfig.getApiSecret());

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());
            JsonNode tokenResponse = responseBody.get("response");

            if (tokenResponse != null && tokenResponse.has("access_token")) {
                return tokenResponse.get("access_token").asText();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // 결제 취소 API
    @PostMapping("/cancel")
    public Map<String, Object> cancelPayment(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        String impUid = (String) request.get("imp_uid");
        String reason = (String) request.get("reason");
        Integer amount = (Integer) request.get("amount"); // 부분취소 금액 (null이면 전액취소)

        if (impUid == null) {
            result.put("success", false);
            result.put("message", "결제 ID가 필요합니다.");
            return result;
        }

        try {
            String accessToken = getAccessToken();

            if (accessToken == null) {
                result.put("success", false);
                result.put("message", "포트원 인증에 실패했습니다.");
                return result;
            }

            String cancelUrl = "https://api.iamport.kr/payments/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("imp_uid", impUid);
            body.put("reason", reason != null ? reason : "고객 요청");
            if (amount != null) {
                body.put("amount", amount);
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(cancelUrl, entity, String.class);

            JsonNode responseBody = objectMapper.readTree(response.getBody());

            if (responseBody.get("code").asInt() == 0) {
                result.put("success", true);
                result.put("message", "결제가 취소되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", responseBody.get("message").asText());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }
}