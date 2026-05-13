package com.kh.shop.controller.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.shop.config.PaymentConfig;
import com.kh.shop.entity.Order;
import com.kh.shop.service.OrderService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment")
public class PaymentApiController {

    @Autowired
    private PaymentConfig paymentConfig;

    @Autowired
    private OrderService orderService;

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
    // 검증 절차:
    //   1) 로그인 확인
    //   2) merchant_uid 로 DB Order 조회 + 소유자/상태 확인
    //   3) 포트원 API 에서 실제 결제 정보 조회
    //   4) 상태=paid, merchant_uid 일치, 금액=order.finalPrice 확인
    //   5) 통과 시 Order PAID 전환. 실패 시 포트원 결제 환불 + Order 취소(재고 복구)
    @PostMapping("/verify")
    public Map<String, Object> verifyPayment(@RequestBody Map<String, Object> request,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 1. 인증 체크
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        String impUid = (String) request.get("imp_uid");
        String merchantUid = (String) request.get("merchant_uid");

        if (impUid == null || impUid.isBlank() || merchantUid == null || merchantUid.isBlank()) {
            result.put("success", false);
            result.put("message", "필수 파라미터가 누락되었습니다.");
            return result;
        }

        // 2. DB Order 조회 + 소유권 확인
        Order order = orderService.getOrderByNumber(merchantUid).orElse(null);
        if (order == null) {
            log.warn("[PAY] 존재하지 않는 주문번호로 검증 시도: user={}, merchantUid={}", userId, merchantUid);
            result.put("success", false);
            result.put("message", "주문을 찾을 수 없습니다.");
            return result;
        }
        if (order.getUser() == null || !userId.equals(order.getUser().getUserId())) {
            log.warn("[PAY] 타인의 주문에 대한 결제 검증 시도: user={}, orderUser={}, merchantUid={}",
                    userId, order.getUser() != null ? order.getUser().getUserId() : null, merchantUid);
            result.put("success", false);
            result.put("message", "주문 정보가 일치하지 않습니다.");
            return result;
        }

        // 3. PENDING 상태인지 확인 (중복/리플레이 방지)
        if (!"PENDING".equals(order.getOrderStatus()) || !"PENDING".equals(order.getPaymentStatus())) {
            log.warn("[PAY] 이미 처리된 주문에 대한 재검증 시도: orderId={}, status={}, paymentStatus={}",
                    order.getOrderId(), order.getOrderStatus(), order.getPaymentStatus());
            result.put("success", false);
            result.put("message", "이미 처리된 주문입니다.");
            return result;
        }

        String accessToken = null;
        try {
            // 4. 포트원 액세스 토큰 발급
            accessToken = getAccessToken();
            if (accessToken == null) {
                result.put("success", false);
                result.put("message", "결제 인증에 실패했습니다.");
                return result;
            }

            // 5. 포트원에서 실제 결제 정보 조회
            JsonNode paymentResponse = fetchPaymentFromPortone(impUid, accessToken);
            if (paymentResponse == null) {
                result.put("success", false);
                result.put("message", "결제 정보를 찾을 수 없습니다.");
                return result;
            }

            int paidAmount = paymentResponse.get("amount").asInt();
            String status = paymentResponse.get("status").asText();
            String portoneMerchantUid = paymentResponse.hasNonNull("merchant_uid")
                    ? paymentResponse.get("merchant_uid").asText() : null;

            // 6. 결제 상태 확인
            if (!"paid".equals(status)) {
                log.warn("[PAY] 결제 미완료 상태: impUid={}, status={}", impUid, status);
                safeCancelOrder(order, "결제 미완료: " + status);
                result.put("success", false);
                result.put("message", "결제가 완료되지 않았습니다.");
                return result;
            }

            // 7. merchant_uid 일치 확인 (포트원 응답 vs 요청 vs DB)
            if (portoneMerchantUid == null || !portoneMerchantUid.equals(merchantUid)) {
                log.warn("[PAY] merchant_uid 불일치: portone={}, request={}", portoneMerchantUid, merchantUid);
                safeCancelPortonePayment(impUid, "주문번호 불일치", accessToken);
                safeCancelOrder(order, "주문번호 불일치");
                result.put("success", false);
                result.put("message", "주문번호가 일치하지 않습니다.");
                return result;
            }

            // 8. 금액 검증 - DB Order 의 finalPrice (서버 계산값) 와 포트원 실제 결제 금액 비교
            //    클라이언트가 보낸 amount 는 사용하지 않는다 (위변조 가능)
            if (paidAmount != order.getFinalPrice()) {
                log.warn("[PAY] 결제 금액 위변조 의심: orderId={}, dbAmount={}, paidAmount={}",
                        order.getOrderId(), order.getFinalPrice(), paidAmount);
                safeCancelPortonePayment(impUid, "결제 금액 위변조 의심", accessToken);
                safeCancelOrder(order, "결제 금액 위변조 의심");
                result.put("success", false);
                result.put("message", "결제 금액이 일치하지 않습니다.");
                return result;
            }

            // 9. 검증 통과 - Order PAID 전환
            orderService.markOrderPaid(order.getOrderId());

            result.put("success", true);
            result.put("orderId", order.getOrderId());
            result.put("orderNumber", order.getOrderNumber());
            result.put("amount", paidAmount);
            result.put("message", "결제가 검증되었습니다.");
            return result;

        } catch (IllegalStateException dup) {
            // markOrderPaid 가 거부 (다른 요청이 먼저 처리한 경우)
            log.warn("[PAY] 동시성/중복 처리: {}", dup.getMessage());
            result.put("success", false);
            result.put("message", "이미 처리된 주문입니다.");
            return result;
        } catch (Exception e) {
            log.error("[PAY] 결제 검증 중 예외 발생: orderId={}, impUid={}",
                    order.getOrderId(), impUid, e);
            // 안전 측: 포트원 환불 + Order 취소
            if (accessToken != null) {
                safeCancelPortonePayment(impUid, "검증 처리 중 오류", accessToken);
            }
            safeCancelOrder(order, "검증 처리 중 오류");
            result.put("success", false);
            result.put("message", "결제 검증 중 오류가 발생했습니다.");
            return result;
        }
    }

    private JsonNode fetchPaymentFromPortone(String impUid, String accessToken) throws Exception {
        String paymentUrl = "https://api.iamport.kr/payments/" + impUid;
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(paymentUrl, HttpMethod.GET, entity, String.class);
        JsonNode paymentData = objectMapper.readTree(response.getBody());
        return paymentData.get("response");
    }

    private void safeCancelPortonePayment(String impUid, String reason, String accessToken) {
        try {
            String cancelUrl = "https://api.iamport.kr/payments/cancel";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> body = new HashMap<>();
            body.put("imp_uid", impUid);
            body.put("reason", reason);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(cancelUrl, entity, String.class);
            log.info("[PAY] 포트원 결제 환불 요청 완료: impUid={}, reason={}", impUid, reason);
        } catch (Exception e) {
            log.error("[PAY] 포트원 결제 환불 실패: impUid={}, reason={}", impUid, reason, e);
        }
    }

    private void safeCancelOrder(Order order, String reason) {
        try {
            orderService.cancelPendingOrder(order.getOrderId(), reason);
        } catch (Exception e) {
            log.error("[PAY] 주문 취소 실패: orderId={}, reason={}", order.getOrderId(), reason, e);
        }
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
            log.error("[PAY] 포트원 액세스 토큰 발급 실패", e);
        }

        return null;
    }

    // 결제 취소 API - 관리자/주문 취소 시 사용
    @PostMapping("/cancel")
    public Map<String, Object> cancelPayment(@RequestBody Map<String, Object> request,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        // 인증/권한 체크 - 관리자만 임의 결제 취소 가능
        String userId = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");
        if (userId == null || !"ADMIN".equals(userRole)) {
            result.put("success", false);
            result.put("message", "권한이 없습니다.");
            return result;
        }

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
            log.error("[PAY] 결제 취소 중 오류", e);
            result.put("success", false);
            result.put("message", "결제 취소 중 오류가 발생했습니다.");
        }

        return result;
    }
}
