package com.kh.shop.security;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 세션 이벤트 리스너
 * - 세션 생성/삭제 이벤트 처리
 * - 세션 레지스트리와 연동하여 중복 로그인 방지
 */
@Slf4j
@Component
public class SessionEventListener implements HttpSessionListener {

    private static SessionRegistry sessionRegistry;

    @Autowired
    public void setSessionRegistry(SessionRegistry registry) {
        SessionEventListener.sessionRegistry = registry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.debug("[세션생성] 세션ID: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.debug("[세션소멸] 세션ID: {}", se.getSession().getId());

        if (sessionRegistry != null) {
            sessionRegistry.removeSession(se.getSession());
        }
    }
}
