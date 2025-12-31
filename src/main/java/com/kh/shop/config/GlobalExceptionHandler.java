package com.kh.shop.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 일반 예외 처리
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, HttpServletRequest request, Model model) {
        model.addAttribute("errorTitle", "오류가 발생했습니다");
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("errorDetail", e.getClass().getSimpleName());
        model.addAttribute("requestUrl", request.getRequestURI());

        // 이전 페이지 URL (Referer)
        String referer = request.getHeader("Referer");
        model.addAttribute("backUrl", referer != null ? referer : "/");

        // Admin 영역인지 확인
        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("isAdmin", isAdmin);

        return isAdmin ? "admin/error" : "client/error";
    }

    // 404 에러 처리
    @ExceptionHandler(NoHandlerFoundException.class)
    public String handleNotFound(NoHandlerFoundException e, HttpServletRequest request, Model model) {
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
    public String handleIllegalArgument(IllegalArgumentException e, HttpServletRequest request, Model model) {
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
    public String handleNullPointer(NullPointerException e, HttpServletRequest request, Model model) {
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
}