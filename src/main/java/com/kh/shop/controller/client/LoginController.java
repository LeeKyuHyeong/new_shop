package com.kh.shop.controller.client;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.User;
import com.kh.shop.security.SessionRegistry;
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

    @Autowired
    private SessionRegistry sessionRegistry;

    @GetMapping("/login")
    public String loginPage(Model model) {
        // 상단 메뉴용 카테고리 조회
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "client/login";
    }

    /**
     * 중복 로그인 체크 API
     */
    @ResponseBody
    @PostMapping("/api/check-duplicate-login")
    public Map<String, Object> checkDuplicateLogin(@RequestParam String userId,
                                                    @RequestParam String userPassword) {
        Map<String, Object> response = new HashMap<>();

        // 먼저 아이디/비밀번호 확인
        Optional<User> user = userService.loginUser(userId, userPassword);
        if (!user.isPresent()) {
            response.put("success", false);
            response.put("message", "아이디 또는 비밀번호를 확인하세요");
            return response;
        }

        // 이미 로그인된 세션이 있는지 확인
        boolean isAlreadyLoggedIn = sessionRegistry.isUserLoggedIn(userId);
        response.put("success", true);
        response.put("alreadyLoggedIn", isAlreadyLoggedIn);

        return response;
    }

    @PostMapping("/login")
    public String login(@RequestParam String userId,
                        @RequestParam String userPassword,
                        @RequestParam(defaultValue = "false") boolean forceLogin,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {

        Optional<User> user = userService.loginUser(userId, userPassword);

        if (user.isPresent()) {
            // 중복 로그인 체크
            boolean isAlreadyLoggedIn = sessionRegistry.isUserLoggedIn(userId);

            if (isAlreadyLoggedIn && !forceLogin) {
                // 강제 로그인이 아니면 확인 요청
                redirectAttributes.addFlashAttribute("confirmDuplicateLogin", true);
                redirectAttributes.addFlashAttribute("pendingUserId", userId);
                return "redirect:/login";
            }

            // 기존 세션 무효화하고 새 세션 등록
            boolean hadExistingSession = sessionRegistry.registerSession(userId, session);
            if (hadExistingSession) {
                // 로그아웃된 사용자에게 알림을 위해 기록
                sessionRegistry.recordForcedLogout(userId);
            }

            session.setAttribute("loggedInUser", userId);
            session.setAttribute("userRole", user.get().getUserRole());
            session.setAttribute("loginTime", System.currentTimeMillis());

            return "redirect:/";
        } else {
            redirectAttributes.addFlashAttribute("loginError", "아이디 또는 비밀번호를 확인하세요");
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 레지스트리에서 제거
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId != null) {
            sessionRegistry.removeSessionByUserId(userId);
        }
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