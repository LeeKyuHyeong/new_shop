package com.kh.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    // 포트원(아임포트) 설정
    @Value("${payment.portone.imp-code:imp00000000}")
    private String impCode;

    @Value("${payment.portone.api-key:}")
    private String apiKey;

    @Value("${payment.portone.api-secret:}")
    private String apiSecret;

    // 테스트 모드 여부
    @Value("${payment.test-mode:true}")
    private boolean testMode;

    public String getImpCode() {
        return impCode;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public boolean isTestMode() {
        return testMode;
    }
}