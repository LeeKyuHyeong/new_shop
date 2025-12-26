package com.kh.shop.controller.admin;

import com.kh.shop.entity.User;
import com.kh.shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserApiController {

    @Autowired
    private UserService userService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/role/{userId}")
    public Map<String, Object> updateUserRole(
            @PathVariable String userId,
            @RequestParam String role,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        // 유효한 역할인지 확인
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            response.put("success", false);
            response.put("message", "유효하지 않은 권한입니다");
            return response;
        }

        try {
            User user = userService.updateUserRole(userId, role);
            if (user != null) {
                response.put("success", true);
                response.put("message", "권한이 변경되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "권한 변경 중 오류가 발생했습니다");
            return response;
        }
    }

    @PostMapping("/status/{userId}")
    public Map<String, Object> updateUserStatus(
            @PathVariable String userId,
            @RequestParam String useYn,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (!"Y".equals(useYn) && !"N".equals(useYn)) {
            response.put("success", false);
            response.put("message", "유효하지 않은 상태입니다");
            return response;
        }

        try {
            User user = userService.updateUserStatus(userId, useYn);
            if (user != null) {
                response.put("success", true);
                response.put("message", "Y".equals(useYn) ? "계정이 활성화되었습니다" : "계정이 비활성화되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상태 변경 중 오류가 발생했습니다");
            return response;
        }
    }

    @PostMapping("/reset-password/{userId}")
    public Map<String, Object> resetPassword(
            @PathVariable String userId,
            @RequestParam String newPassword,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (newPassword == null || newPassword.length() < 4) {
            response.put("success", false);
            response.put("message", "비밀번호는 4자 이상이어야 합니다");
            return response;
        }

        try {
            User user = userService.resetPassword(userId, newPassword);
            if (user != null) {
                response.put("success", true);
                response.put("message", "비밀번호가 초기화되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "사용자를 찾을 수 없습니다");
            }
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 초기화 중 오류가 발생했습니다");
            return response;
        }
    }
}