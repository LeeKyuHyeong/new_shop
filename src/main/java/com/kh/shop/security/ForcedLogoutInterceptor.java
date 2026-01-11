package com.kh.shop.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * 강제 로그아웃된 사용자에게 알림을 표시하는 인터셉터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForcedLogoutInterceptor implements HandlerInterceptor {

    private final SessionRegistry sessionRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        // 로그인되지 않은 상태에서 로그인 페이지 접근 시
        if (session == null || session.getAttribute("loggedInUser") == null) {
            // 요청 파라미터에서 userId 확인 (로그인 시도 후 리다이렉트된 경우)
            String userId = request.getParameter("forcedLogoutUser");
            if (userId != null && sessionRegistry.checkAndClearForcedLogout(userId)) {
                // Flash attribute로 메시지 전달
                FlashMap flashMap = new FlashMap();
                flashMap.put("forcedLogoutMessage", "다른 기기에서 로그인하여 현재 세션이 종료되었습니다.");
                FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
                if (flashMapManager != null) {
                    flashMapManager.saveOutputFlashMap(flashMap, request, response);
                }
                log.info("[강제로그아웃] 알림 표시 - 사용자: {}", userId);
            }
        }

        return true;
    }
}
