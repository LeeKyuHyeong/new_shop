package com.kh.shop.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 보안 헤더 설정 필터
 * - X-Frame-Options: 클릭재킹 방지
 * - X-Content-Type-Options: MIME 스니핑 방지
 * - X-XSS-Protection: 브라우저 XSS 필터 활성화
 * - Content-Security-Policy: 리소스 로딩 제한
 * - Referrer-Policy: 리퍼러 정보 제한
 * - Permissions-Policy: 브라우저 기능 제한
 */
@Component
@Order(1)
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 클릭재킹 방지 - 같은 도메인에서만 iframe 허용
        httpResponse.setHeader("X-Frame-Options", "SAMEORIGIN");

        // MIME 타입 스니핑 방지
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 브라우저 XSS 필터 활성화
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://cdn.jsdelivr.net https://code.jquery.com https://cdn.iamport.kr; " +
                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com; " +
                "font-src 'self' https://fonts.gstatic.com https://cdn.jsdelivr.net; " +
                "img-src 'self' data: https: blob:; " +
                "connect-src 'self' https://api.iamport.kr https://kapi.kakao.com https://nid.naver.com https://accounts.google.com; " +
                "frame-src 'self' https://service.iamport.kr https://kauth.kakao.com https://nid.naver.com https://accounts.google.com;");

        // 리퍼러 정책
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // 권한 정책 (카메라, 마이크 등 제한)
        httpResponse.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        chain.doFilter(request, response);
    }
}
