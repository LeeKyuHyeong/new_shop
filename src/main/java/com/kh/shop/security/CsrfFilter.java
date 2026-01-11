package com.kh.shop.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

/**
 * CSRF (Cross-Site Request Forgery) 방지 필터
 * - 상태 변경 요청(POST, PUT, DELETE)에 대해 CSRF 토큰 검증
 * - API 엔드포인트와 안전한 메서드(GET, HEAD, OPTIONS)는 제외
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
public class CsrfFilter implements Filter {

    private final CsrfTokenManager csrfTokenManager;

    // CSRF 검증을 건너뛸 경로 (API, 웹훅 등)
    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/api/payment/webhook",      // 결제 웹훅
            "/api/social/callback"       // 소셜 로그인 콜백
    );

    // 안전한 HTTP 메서드 (상태 변경 없음)
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String method = httpRequest.getMethod();
        String path = httpRequest.getRequestURI();

        // 안전한 메서드는 검증 제외
        if (SAFE_METHODS.contains(method.toUpperCase())) {
            chain.doFilter(request, response);
            return;
        }

        // 제외 경로 확인
        if (isExcludedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        // API 요청은 별도 처리 (API는 주로 토큰 인증 사용)
        if (path.startsWith("/api/")) {
            // X-Requested-With 헤더가 있으면 AJAX 요청으로 간주하고 통과
            String xRequestedWith = httpRequest.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(xRequestedWith)) {
                // AJAX 요청의 경우 CSRF 토큰 검증
                if (!csrfTokenManager.validateToken(httpRequest)) {
                    log.warn("[CSRF] 토큰 검증 실패 - Path: {}, Method: {}", path, method);
                    httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    httpResponse.setContentType("application/json;charset=UTF-8");
                    httpResponse.getWriter().write("{\"error\":\"CSRF token validation failed\",\"message\":\"잘못된 요청입니다. 페이지를 새로고침 후 다시 시도해주세요.\"}");
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        // 폼 제출의 경우 CSRF 토큰 검증
        if (!csrfTokenManager.validateToken(httpRequest)) {
            log.warn("[CSRF] 토큰 검증 실패 - Path: {}, Method: {}", path, method);

            // 관리자 페이지인 경우
            if (path.startsWith("/admin")) {
                httpResponse.sendRedirect("/admin?error=csrf");
            } else {
                httpResponse.sendRedirect("/?error=csrf");
            }
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }
}
