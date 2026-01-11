package com.kh.shop.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 * - 민감정보 노출 방지
 * - 사용자 친화적 에러 메시지 제공
 * - 상세 에러는 로그에만 기록
 */
@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    // 개발 환경에서만 상세 에러 표시
    private boolean isDevMode() {
        return "dev".equals(activeProfile) || "local".equals(activeProfile);
    }

    // API 요청인지 확인
    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        return request.getRequestURI().startsWith("/api/") ||
                "XMLHttpRequest".equals(xRequestedWith) ||
                (accept != null && accept.contains("application/json") && !accept.contains("text/html"));
    }

    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception e, HttpServletRequest request, Model model) {
        // 에러 로깅 (스택트레이스 포함)
        log.error("[Exception] URI: {}, Error: {}", request.getRequestURI(), e.getMessage(), e);

        // API 요청인 경우 JSON 응답
        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "SERVER_ERROR");

            if (isDevMode()) {
                errorResponse.put("message", e.getMessage());
                errorResponse.put("type", e.getClass().getSimpleName());
            } else {
                errorResponse.put("message", "서비스 처리 중 오류가 발생했습니다.");
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // 페이지 요청인 경우 에러 페이지
        model.addAttribute("errorTitle", "오류가 발생했습니다");

        // 프로덕션 환경에서는 일반적인 메시지만 표시
        if (isDevMode()) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("errorDetail", e.getClass().getSimpleName());
        } else {
            model.addAttribute("errorMessage", "서비스 이용 중 문제가 발생했습니다. 잠시 후 다시 시도해주세요.");
            model.addAttribute("errorDetail", "관리자에게 문의해주세요.");
        }

        model.addAttribute("requestUrl", request.getRequestURI());

        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer != null ? referer : "/");

        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }

    // 404 에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException e, HttpServletRequest request, Model model) {
        log.warn("[404] URI: {}", request.getRequestURI());

        model.addAttribute("errorTitle", "페이지를 찾을 수 없습니다");
        model.addAttribute("errorMessage", "요청하신 페이지가 존재하지 않거나 삭제되었습니다.");
        model.addAttribute("errorCode", "404");
        model.addAttribute("requestUrl", request.getRequestURI());
        model.addAttribute("backUrl", "/");

        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }

    // IllegalArgumentException 처리 (잘못된 파라미터)
    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request, Model model) {
        log.warn("[IllegalArgument] URI: {}, Message: {}", request.getRequestURI(), e.getMessage());

        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "INVALID_ARGUMENT");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        model.addAttribute("errorTitle", "잘못된 요청입니다");
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorDetail", "입력값을 확인해주세요.");
        model.addAttribute("requestUrl", request.getRequestURI());

        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer != null ? referer : "/");

        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }

    // NullPointerException 처리
    @ExceptionHandler(NullPointerException.class)
    public Object handleNullPointer(NullPointerException e, HttpServletRequest request, Model model) {
        log.error("[NullPointer] URI: {}", request.getRequestURI(), e);

        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "NOT_FOUND");
            errorResponse.put("message", "요청하신 정보를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        model.addAttribute("errorTitle", "데이터를 찾을 수 없습니다");
        model.addAttribute("errorMessage", "요청하신 정보가 존재하지 않습니다.");
        model.addAttribute("errorDetail", "데이터가 삭제되었거나 잘못된 접근입니다.");
        model.addAttribute("requestUrl", request.getRequestURI());

        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer != null ? referer : "/");

        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }

    // 보안 관련 예외 처리
    @ExceptionHandler(SecurityException.class)
    public Object handleSecurityException(SecurityException e, HttpServletRequest request, Model model) {
        log.warn("[Security] URI: {}, IP: {}, Message: {}",
                request.getRequestURI(), request.getRemoteAddr(), e.getMessage());

        if (isApiRequest(request)) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "ACCESS_DENIED");
            errorResponse.put("message", "접근이 거부되었습니다.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        model.addAttribute("errorTitle", "접근이 거부되었습니다");
        model.addAttribute("errorMessage", "해당 페이지에 접근할 권한이 없습니다.");
        model.addAttribute("backUrl", "/");

        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }
}
