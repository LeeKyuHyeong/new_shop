package com.kh.shop.controller.client;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.UserService;
import com.kh.shop.service.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class ClientSettingController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserSettingService userSettingService;

    // 마이페이지 메인 (주문내역으로 리다이렉트)
    @GetMapping("/mypage")
    public String mypageMain() {
        return "redirect:/mypage/orders";
    }

    // 개인설정 페이지
    @GetMapping("/mypage/setting")
    public String settingPage(HttpSession session, Model model) {
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            return "redirect:/login";
        }

        // 상단 메뉴용 카테고리
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        // 사용자 정보
        Optional<User> userOpt = userService.getUserByUserId(userId);
        if (userOpt.isEmpty()) {
            return "redirect:/login";
        }
        model.addAttribute("user", userOpt.get());

        // 사용자 설정 (없으면 생성)
        UserSetting setting = userSettingService.getOrCreateSetting(userId);
        model.addAttribute("setting", setting);

        return "client/mypage/setting";
    }

    // 테마 변경 API
    @PostMapping("/api/setting/theme")
    @ResponseBody
    public Map<String, Object> updateTheme(@RequestParam String theme, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");

        // 비로그인 사용자는 클라이언트에서 localStorage로 처리
        if (userId == null) {
            response.put("success", true);
            response.put("message", "로컬 테마 변경");
            response.put("saved", false);
            return response;
        }

        try {
            UserSetting setting = userSettingService.updateTheme(userId, theme);
            if (setting != null) {
                response.put("success", true);
                response.put("message", "테마가 변경되었습니다");
                response.put("saved", true);
                response.put("theme", setting.getTheme());
            } else {
                response.put("success", false);
                response.put("message", "설정을 찾을 수 없습니다");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "테마 변경 중 오류가 발생했습니다");
        }

        return response;
    }

    // 현재 테마 조회 API
    @GetMapping("/api/setting/theme")
    @ResponseBody
    public Map<String, Object> getTheme(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        String theme = userSettingService.getTheme(userId);

        response.put("success", true);
        response.put("theme", theme);
        response.put("loggedIn", userId != null);

        return response;
    }

    // 전체 설정 저장 API
    @PostMapping("/api/setting/save")
    @ResponseBody
    public Map<String, Object> saveSettings(
            @RequestParam(required = false) String theme,
            @RequestParam(required = false) String notificationYn,
            @RequestParam(required = false) String emailReceiveYn,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다");
            return response;
        }

        try {
            UserSetting setting = userSettingService.updateAllSettings(userId, theme, notificationYn, emailReceiveYn);
            if (setting != null) {
                response.put("success", true);
                response.put("message", "설정이 저장되었습니다");
            } else {
                response.put("success", false);
                response.put("message", "설정 저장에 실패했습니다");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "설정 저장 중 오류가 발생했습니다");
        }

        return response;
    }

    // 비밀번호 변경 API
    @PostMapping("/api/setting/password")
    @ResponseBody
    public Map<String, Object> changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다");
            return response;
        }

        // 새 비밀번호 확인
        if (!newPassword.equals(confirmPassword)) {
            response.put("success", false);
            response.put("message", "새 비밀번호가 일치하지 않습니다");
            return response;
        }

        // 현재 비밀번호 확인
        Optional<User> userOpt = userService.loginUser(userId, currentPassword);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "현재 비밀번호가 올바르지 않습니다");
            return response;
        }

        try {
            userService.resetPassword(userId, newPassword);
            response.put("success", true);
            response.put("message", "비밀번호가 변경되었습니다");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "비밀번호 변경 중 오류가 발생했습니다");
        }

        return response;
    }
}