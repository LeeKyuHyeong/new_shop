package com.kh.shop.controller.admin;

import com.kh.shop.entity.User;
import com.kh.shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender mailSender;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        return loggedInUser != null && "ADMIN".equals(userRole);
    }

    // BCrypt 마이그레이션 진단 - 평문 비밀번호 남은 사용자 수.
    // 0 에 도달하면 UserService.loginUser 의 평문 분기 정리 가능.
    @GetMapping("/migration/plain-password-count")
    public Map<String, Object> getPlainPasswordCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }
        response.put("success", true);
        response.put("count", userService.countPlainPasswordUsers());
        response.put("sunsetDate", userService.getBcryptSunsetDate());
        return response;
    }

    /**
     * 평문 비밀번호 사용자에게 비밀번호 변경 권고 메일을 일괄 발송.
     * 운영자 수동 트리거. 일정 기간 후 재발송 가능.
     */
    @PostMapping("/migration/send-warning-emails")
    public Map<String, Object> sendPlainPasswordWarningEmails(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        LocalDate sunsetDate = userService.getBcryptSunsetDate();
        List<User> targets = userService.findPlainPasswordUsers();

        int sent = 0;
        int failed = 0;
        for (User user : targets) {
            String email = user.getEmail();
            if (email == null || email.isBlank()) {
                failed++;
                continue;
            }
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(email);
                message.setSubject("[KH SHOP] 보안 정책 변경 안내 - 비밀번호 재설정 권고");
                message.setText(buildWarningEmailBody(user, sunsetDate));
                mailSender.send(message);
                sent++;
            } catch (Exception e) {
                log.warn("[BCRYPT-SUNSET] 권고 메일 발송 실패. userId={}, email={}, cause={}",
                        user.getUserId(), email, e.getMessage());
                failed++;
            }
        }

        log.info("[BCRYPT-SUNSET] 권고 메일 발송 완료. 대상={}, 성공={}, 실패={}, sunsetDate={}",
                targets.size(), sent, failed, sunsetDate);

        response.put("success", true);
        response.put("targetCount", targets.size());
        response.put("sentCount", sent);
        response.put("failedCount", failed);
        response.put("sunsetDate", sunsetDate);
        return response;
    }

    private String buildWarningEmailBody(User user, LocalDate sunsetDate) {
        long daysLeft = sunsetDate == null ? -1L
                : ChronoUnit.DAYS.between(LocalDate.now(), sunsetDate);

        StringBuilder sb = new StringBuilder();
        sb.append("안녕하세요, ").append(user.getUserName() == null ? user.getUserId() : user.getUserName()).append("님.\n\n");
        sb.append("KH SHOP 보안 정책 변경에 따라 기존 비밀번호의 사용이 곧 중단됩니다.\n\n");
        if (sunsetDate != null) {
            sb.append("- 사용 중단 예정일: ").append(sunsetDate).append("\n");
            if (daysLeft >= 0) {
                sb.append("- 남은 기간: 약 ").append(daysLeft).append("일\n");
            }
        }
        sb.append("- 대상 아이디: ").append(user.getUserId()).append("\n\n");
        sb.append("아래 안내에 따라 사용 중단 전에 비밀번호를 변경해주시기 바랍니다.\n");
        sb.append("  1) KH SHOP 에 로그인합니다 (현재 비밀번호로 가능).\n");
        sb.append("  2) [마이페이지] → [개인설정] → [비밀번호 변경] 에서 새 비밀번호를 설정합니다.\n\n");
        sb.append("사용 중단일이 지나면 현재 비밀번호로 더 이상 로그인할 수 없으며, 관리자에게 비밀번호 초기화를 요청해야 합니다.\n\n");
        sb.append("문의: KH SHOP 고객센터\n");
        sb.append("본인이 KH SHOP 계정을 만든 적이 없다면 이 메일을 무시해주세요.\n");
        return sb.toString();
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