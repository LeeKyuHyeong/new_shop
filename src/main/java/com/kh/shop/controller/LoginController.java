package com.kh.shop.controller;

import com.kh.shop.entity.User;
import com.kh.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String userPassword,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional<User> user = userService.loginUser(userId, userPassword);

        if (user.isPresent()) {
            session.setAttribute("loggedInUser", userId);
            session.setAttribute("loginTime", System.currentTimeMillis());
            return "redirect:/index";
        } else {
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호를 확인하세요");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup";
    }

    @PostMapping("/signup")
    public Map<String, Object> signup(@RequestParam String userId,
                         @RequestParam String userPassword,
                         @RequestParam String confirmPassword,
                         @RequestParam String userName,
                         @RequestParam String email,
                         @RequestParam(required = false) String gender,
                         @RequestParam(required = false) String birth,
                         RedirectAttributes redirectAttributes) {

        Map<String, Object> response = new HashMap<>();

        // 비밀번호 일치 확인
        if (!userPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "비밀번호가 일치하지 않습니다");
            return response;
        }

        // 아이디 중복 확인
        if (userService.isDuplicateUserId(userId)) {
            response.put("success", false);
            response.put("message", "이미 사용 중인 아이디입니다");
            return response;
        }

        // 이메일 중복 확인
        if (userService.isDuplicateEmail(email)) {
            response.put("success", false);
            response.put("message", "이미 사용 중인 이메일입니다");
            return response;
        }

        // 회원가입 처리
        try {
            LocalDate birthDate = null;
            if (birth != null && !birth.isEmpty()) {
                birthDate = LocalDate.parse(birth);
            }

            userService.registerUser(userId, userPassword, userName, email, gender, birthDate);
            response.put("success", true);
            response.put("message", "회원가입에 성공했습니다");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "회원가입 중 오류가 발생했습니다");
            return response;
        }
    }
}