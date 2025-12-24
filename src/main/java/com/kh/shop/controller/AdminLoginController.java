package com.kh.shop.controller;

import com.kh.shop.entity.User;
import com.kh.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminLoginController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String adminLoginPage(HttpSession session) {
        // 이미 로그인되어 있고 ADMIN이면 admin 메인으로
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        if (loggedInUser != null && "ADMIN".equals(userRole)) {
            return "redirect:/admin";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String userId,
                             @RequestParam String userPassword,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        Optional<User> user = userService.loginUser(userId, userPassword);

        if (user.isPresent()) {
            // ADMIN 권한 체크
            if ("ADMIN".equals(user.get().getUserRole())) {
                session.setAttribute("loggedInUser", userId);
                session.setAttribute("userRole", user.get().getUserRole());
                session.setAttribute("loginTime", System.currentTimeMillis());
                return "redirect:/admin";
            } else {
                redirectAttributes.addFlashAttribute("loginError", "관리자 권한이 없습니다");
                return "redirect:/admin/login";
            }
        } else {
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호를 확인하세요");
            return "redirect:/admin/login";
        }
    }

    @GetMapping("/logout")
    public String adminLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login";
    }
}