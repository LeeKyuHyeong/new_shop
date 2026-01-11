package com.kh.shop.config;

import com.kh.shop.security.CsrfInterceptor;
import com.kh.shop.security.ForcedLogoutInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CsrfInterceptor csrfInterceptor;
    private final ForcedLogoutInterceptor forcedLogoutInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // CSRF 토큰 자동 주입 인터셉터
        registry.addInterceptor(csrfInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico");

        // 강제 로그아웃 알림 인터셉터
        registry.addInterceptor(forcedLogoutInterceptor)
                .addPathPatterns("/login");
    }
}
