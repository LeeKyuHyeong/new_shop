package com.kh.shop.security;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 사용자별 세션 관리 레지스트리
 * - 중복 로그인 방지를 위해 사용자당 하나의 세션만 유지
 * - 새로운 로그인 시 기존 세션 무효화
 */
@Slf4j
@Component
public class SessionRegistry {

    // 사용자 ID -> 세션 매핑
    private final Map<String, HttpSession> userSessions = new ConcurrentHashMap<>();

    // 세션 ID -> 사용자 ID 역매핑 (세션 만료 시 정리용)
    private final Map<String, String> sessionUsers = new ConcurrentHashMap<>();

    /**
     * 사용자 로그인 시 세션 등록
     * 기존 세션이 있으면 무효화하고 새 세션 등록
     *
     * @param userId 사용자 ID
     * @param session 새 세션
     * @return 기존 세션이 있었으면 true
     */
    public boolean registerSession(String userId, HttpSession session) {
        if (userId == null || session == null) {
            return false;
        }

        boolean hadExistingSession = false;

        // 기존 세션이 있으면 무효화
        HttpSession existingSession = userSessions.get(userId);
        if (existingSession != null && !existingSession.getId().equals(session.getId())) {
            try {
                log.info("[중복로그인] 기존 세션 무효화 - 사용자: {}, 기존세션: {}, 새세션: {}",
                        userId, existingSession.getId(), session.getId());

                // 기존 세션에 중복 로그인 알림 플래그 설정
                existingSession.setAttribute("duplicateLogin", true);
                existingSession.setAttribute("duplicateLoginMessage", "다른 기기에서 로그인하여 현재 세션이 종료되었습니다.");

                // 기존 세션 무효화
                existingSession.invalidate();
                hadExistingSession = true;
            } catch (IllegalStateException e) {
                // 이미 무효화된 세션
                log.debug("[중복로그인] 기존 세션이 이미 무효화됨 - 사용자: {}", userId);
            }

            // 역매핑에서 기존 세션 제거
            sessionUsers.remove(existingSession.getId());
        }

        // 새 세션 등록
        userSessions.put(userId, session);
        sessionUsers.put(session.getId(), userId);

        log.debug("[세션등록] 사용자: {}, 세션: {}", userId, session.getId());

        return hadExistingSession;
    }

    /**
     * 세션 제거 (로그아웃 또는 세션 만료 시)
     */
    public void removeSession(HttpSession session) {
        if (session == null) {
            return;
        }

        String sessionId = session.getId();
        String userId = sessionUsers.remove(sessionId);

        if (userId != null) {
            // 현재 등록된 세션이 이 세션인 경우에만 제거
            HttpSession registeredSession = userSessions.get(userId);
            if (registeredSession != null && registeredSession.getId().equals(sessionId)) {
                userSessions.remove(userId);
                log.debug("[세션제거] 사용자: {}, 세션: {}", userId, sessionId);
            }
        }
    }

    /**
     * 사용자 ID로 세션 제거
     */
    public void removeSessionByUserId(String userId) {
        if (userId == null) {
            return;
        }

        HttpSession session = userSessions.remove(userId);
        if (session != null) {
            sessionUsers.remove(session.getId());
            log.debug("[세션제거] 사용자: {}", userId);
        }
    }

    /**
     * 사용자의 현재 세션 조회
     */
    public HttpSession getSession(String userId) {
        return userSessions.get(userId);
    }

    /**
     * 사용자가 로그인 중인지 확인
     */
    public boolean isUserLoggedIn(String userId) {
        HttpSession session = userSessions.get(userId);
        if (session == null) {
            return false;
        }

        try {
            // 세션이 유효한지 확인 (getAttribute 호출로 검증)
            session.getAttribute("loggedInUser");
            return true;
        } catch (IllegalStateException e) {
            // 세션이 무효화됨
            removeSessionByUserId(userId);
            return false;
        }
    }

    /**
     * 현재 로그인 중인 사용자 수
     */
    public int getActiveSessionCount() {
        return userSessions.size();
    }

    /**
     * 세션 ID로 사용자 ID 조회
     */
    public String getUserIdBySessionId(String sessionId) {
        return sessionUsers.get(sessionId);
    }
}
