package com.kh.shop.controller.common;

import com.kh.shop.service.EmailVerificationService;
import com.kh.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @GetMapping("/check-userid")
    public Map<String, Object> checkUserId(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        boolean duplicate = userService.isDuplicateUserId(userId);
        response.put("duplicate", duplicate);
        return response;
    }

    @GetMapping("/check-email")
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean duplicate = userService.isDuplicateEmail(email);
        response.put("duplicate", duplicate);
        return response;
    }

    @PostMapping("/send-verification-code")
    public Map<String, Object> sendVerificationCode(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        // 이메일 중복 확인
        if (userService.isDuplicateEmail(email)) {
            response.put("success", false);
            response.put("message", "이미 사용 중인 이메일입니다");
            return response;
        }

        try {
            emailVerificationService.sendVerificationCode(email);
            response.put("success", true);
            response.put("message", "인증코드가 발송되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "인증코드 발송에 실패했습니다");
        }
        return response;
    }

    @PostMapping("/verify-email-code")
    public Map<String, Object> verifyEmailCode(@RequestParam String email,
                                                @RequestParam String code) {
        Map<String, Object> response = new HashMap<>();
        boolean verified = emailVerificationService.verifyCode(email, code);

        if (verified) {
            response.put("success", true);
            response.put("message", "이메일 인증이 완료되었습니다");
        } else {
            response.put("success", false);
            response.put("message", "인증코드가 올바르지 않거나 만료되었습니다");
        }
        return response;
    }
}