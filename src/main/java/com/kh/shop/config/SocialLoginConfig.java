package com.kh.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocialLoginConfig {

    // 카카오
    @Value("${social.kakao.client-id:}")
    private String kakaoClientId;

    @Value("${social.kakao.client-secret:}")
    private String kakaoClientSecret;

    @Value("${social.kakao.redirect-uri:}")
    private String kakaoRedirectUri;

    // 네이버
    @Value("${social.naver.client-id:}")
    private String naverClientId;

    @Value("${social.naver.client-secret:}")
    private String naverClientSecret;

    @Value("${social.naver.redirect-uri:}")
    private String naverRedirectUri;

    // 구글
    @Value("${social.google.client-id:}")
    private String googleClientId;

    @Value("${social.google.client-secret:}")
    private String googleClientSecret;

    @Value("${social.google.redirect-uri:}")
    private String googleRedirectUri;

    // Getters
    public String getKakaoClientId() { return kakaoClientId; }
    public String getKakaoClientSecret() { return kakaoClientSecret; }
    public String getKakaoRedirectUri() { return kakaoRedirectUri; }

    public String getNaverClientId() { return naverClientId; }
    public String getNaverClientSecret() { return naverClientSecret; }
    public String getNaverRedirectUri() { return naverRedirectUri; }

    public String getGoogleClientId() { return googleClientId; }
    public String getGoogleClientSecret() { return googleClientSecret; }
    public String getGoogleRedirectUri() { return googleRedirectUri; }
}