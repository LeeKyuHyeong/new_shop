package com.kh.shop.controller;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.User;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class LoginController {

    boolean debug = true;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        // 상단 메뉴용 카테고리 조회
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "client/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String userPassword,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional<User> user = userService.loginUser(userId, userPassword);

        if (user.isPresent()) {
            session.setAttribute("loggedInUser", userId);
            session.setAttribute("userRole", user.get().getUserRole());
            session.setAttribute("loginTime", System.currentTimeMillis());

            // 역할에 따라 분기 (관리자는 따로 '관리자' 버튼으로 이동)
//            if ("ADMIN".equals(user.get().getUserRole())) {
//                return "redirect:/admin";
//            } else {
//
//            }
            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호를 확인하세요");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        // 상단 메뉴용 카테고리 조회
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "client/signup";
    }

    @ResponseBody
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