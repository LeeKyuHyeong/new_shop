package com.kh.shop.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * XSS (Cross-Site Scripting) 방지 필터
 * - 모든 요청 파라미터에서 XSS 공격 패턴 필터링
 * - HTML 특수문자 이스케이프 처리
 */
@Component
@Order(2)
public class XssFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        chain.doFilter(new XssRequestWrapper(httpRequest), response);
    }

    /**
     * XSS 필터링을 적용하는 HttpServletRequest 래퍼
     */
    private static class XssRequestWrapper extends HttpServletRequestWrapper {

        public XssRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String value = super.getParameter(name);
            return sanitize(value);
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) {
                return null;
            }

            String[] sanitizedValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                sanitizedValues[i] = sanitize(values[i]);
            }
            return sanitizedValues;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            Map<String, String[]> originalMap = super.getParameterMap();
            Map<String, String[]> sanitizedMap = new HashMap<>();

            for (Map.Entry<String, String[]> entry : originalMap.entrySet()) {
                String[] values = entry.getValue();
                String[] sanitizedValues = new String[values.length];
                for (int i = 0; i < values.length; i++) {
                    sanitizedValues[i] = sanitize(values[i]);
                }
                sanitizedMap.put(entry.getKey(), sanitizedValues);
            }

            return sanitizedMap;
        }

        @Override
        public String getHeader(String name) {
            String value = super.getHeader(name);
            // Referer, Cookie 등 일부 헤더는 제외
            if ("referer".equalsIgnoreCase(name) || "cookie".equalsIgnoreCase(name)) {
                return value;
            }
            return sanitize(value);
        }

        /**
         * XSS 공격 패턴 제거 및 HTML 이스케이프
         */
        private String sanitize(String value) {
            if (value == null) {
                return null;
            }

            // 위험한 스크립트 패턴 제거
            String sanitized = value;

            // <script> 태그 제거
            sanitized = sanitized.replaceAll("(?i)<script[^>]*>.*?</script>", "");
            sanitized = sanitized.replaceAll("(?i)<script[^>]*>", "");
            sanitized = sanitized.replaceAll("(?i)</script>", "");

            // javascript: 프로토콜 제거
            sanitized = sanitized.replaceAll("(?i)javascript:", "");

            // on* 이벤트 핸들러 제거 (onclick, onerror 등)
            sanitized = sanitized.replaceAll("(?i)\\s+on\\w+\\s*=", " ");

            // HTML 특수문자 이스케이프
            sanitized = HtmlUtils.htmlEscape(sanitized);

            return sanitized;
        }
    }
}
