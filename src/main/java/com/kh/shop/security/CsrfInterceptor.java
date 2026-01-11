package com.kh.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * CSRF 토큰을 모든 뷰에 자동 주입하는 인터셉터
 */
@Component
@RequiredArgsConstructor
public class CsrfInterceptor implements HandlerInterceptor {

    private final CsrfTokenManager csrfTokenManager;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {

        if (modelAndView != null && request.getSession(false) != null) {
            String token = csrfTokenManager.getOrCreateToken(request.getSession());
            modelAndView.addObject("_csrf", token);
            modelAndView.addObject("_csrfHeader", csrfTokenManager.getTokenHeaderName());
            modelAndView.addObject("_csrfParam", csrfTokenManager.getTokenParamName());
        }
    }
}
