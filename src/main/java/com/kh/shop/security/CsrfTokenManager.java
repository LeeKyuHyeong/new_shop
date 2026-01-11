package com.kh.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * CSRF 토큰 관리자
 * - 세션별 고유 토큰 생성 및 검증
 */
@Component
public class CsrfTokenManager {

    private static final String CSRF_TOKEN_SESSION_KEY = "_csrf_token";
    private static final String CSRF_TOKEN_HEADER = "X-CSRF-TOKEN";
    private static final String CSRF_TOKEN_PARAM = "_csrf";
    private static final int TOKEN_LENGTH = 32;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * CSRF 토큰 생성 또는 기존 토큰 반환
     */
    public String getOrCreateToken(HttpSession session) {
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (token == null) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_SESSION_KEY, token);
        }
        return token;
    }

    /**
     * CSRF 토큰 검증
     */
    public boolean validateToken(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_KEY);
        if (sessionToken == null) {
            return false;
        }

        // 헤더에서 토큰 확인 (AJAX 요청)
        String requestToken = request.getHeader(CSRF_TOKEN_HEADER);

        // 파라미터에서 토큰 확인 (폼 제출)
        if (requestToken == null || requestToken.isEmpty()) {
            requestToken = request.getParameter(CSRF_TOKEN_PARAM);
        }

        return sessionToken.equals(requestToken);
    }

    /**
     * 토큰 재생성 (로그인 후 등)
     */
    public String regenerateToken(HttpSession session) {
        String token = generateToken();
        session.setAttribute(CSRF_TOKEN_SESSION_KEY, token);
        return token;
    }

    /**
     * 보안 랜덤 토큰 생성
     */
    private String generateToken() {
        byte[] bytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String getTokenHeaderName() {
        return CSRF_TOKEN_HEADER;
    }

    public String getTokenParamName() {
        return CSRF_TOKEN_PARAM;
    }
}
