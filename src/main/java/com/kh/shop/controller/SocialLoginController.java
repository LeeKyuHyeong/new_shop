package com.kh.shop.controller.client;

import com.kh.shop.entity.User;
import com.kh.shop.service.SocialLoginService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/oauth")
public class SocialLoginController {

    @Autowired
    private SocialLoginService socialLoginService;

    // ==================== 카카오 ====================

    @GetMapping("/kakao")
    public String kakaoLogin() {
        return "redirect:" + socialLoginService.getKakaoAuthUrl();
    }

    @GetMapping("/kakao/callback")
    public String kakaoCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String error,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (error != null || code == null) {
            redirectAttributes.addFlashAttribute("loginError", "카카오 로그인이 취소되었습니다.");
            return "redirect:/login";
        }

        Map<String, Object> result = socialLoginService.kakaoLogin(code);
        return handleOAuthResult(result, "KAKAO", session, redirectAttributes);
    }

    // ==================== 네이버 ====================

    @GetMapping("/naver")
    public String naverLogin(HttpSession session) {
        String state = UUID.randomUUID().toString();
        session.setAttribute("naverState", state);
        return "redirect:" + socialLoginService.getNaverAuthUrl(state);
    }

    @GetMapping("/naver/callback")
    public String naverCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String error,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        if (error != null || code == null) {
            redirectAttributes.addFlashAttribute("loginError", "네이버 로그인이 취소되었습니다.");
            return "redirect:/login";
        }

        // state 검증
        String savedState = (String) session.getAttribute("naverState");
        if (savedState == null || !savedState.equals(state)) {
            redirectAttributes.addFlashAttribute("loginError", "잘못된 요청입니다.");
            return "redirect:/login";
        }
        session.removeAttribute("naverState");

        Map<String, Object> result = socialLoginService.naverLogin(code, state);
        return handleOAuthResult(result, "NAVER", session, redirectAttributes);
    }

    // ==================== 구글 ====================

    @GetMapping("/google")
    public String googleLogin() {
        return "redirect:" + socialLoginService.getGoogleAuthUrl();
    }

    @GetMapping("/google/callback")
    public String googleCallback(@RequestParam(required = false) String code,
                                 @RequestParam(required = false) String error,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        if (error != null || code == null) {
            redirectAttributes.addFlashAttribute("loginError", "구글 로그인이 취소되었습니다.");
            return "redirect:/login";
        }

        Map<String, Object> result = socialLoginService.googleLogin(code);
        return handleOAuthResult(result, "GOOGLE", session, redirectAttributes);
    }

    // ==================== 공통 처리 ====================

    private String handleOAuthResult(Map<String, Object> result, String provider,
                                     HttpSession session, RedirectAttributes redirectAttributes) {

        if (!(Boolean) result.get("success")) {
            redirectAttributes.addFlashAttribute("loginError", (String) result.get("message"));
            return "redirect:/login";
        }

        Boolean isNewUser = (Boolean) result.get("isNewUser");

        if (isNewUser != null && isNewUser) {
            // 신규 사용자 - 세션에 소셜 정보 저장 후 회원가입 페이지로 이동
            session.setAttribute("socialSignup", result);
            return "redirect:/oauth/signup";
        }

        // 기존 사용자 - 로그인 처리
        User user = (User) result.get("user");
        session.setAttribute("loggedInUser", user.getUserId());
        session.setAttribute("userRole", user.getUserRole());
        session.setAttribute("loginTime", System.currentTimeMillis());
        session.setAttribute("socialLogin", provider);

        // 계정 연동 알림
        if (result.get("linked") != null && (Boolean) result.get("linked")) {
            redirectAttributes.addFlashAttribute("message", provider + " 계정이 기존 회원 정보와 연동되었습니다.");
        }

        return "redirect:/";
    }

    // ==================== 소셜 회원가입 ====================

    @GetMapping("/signup")
    public String socialSignupForm(HttpSession session, Model model) {
        Map<String, Object> socialData = (Map<String, Object>) session.getAttribute("socialSignup");

        if (socialData == null) {
            return "redirect:/login";
        }

        model.addAttribute("provider", socialData.get("provider"));
        model.addAttribute("providerId", socialData.get("providerId"));
        model.addAttribute("email", socialData.get("email"));
        model.addAttribute("nickname", socialData.get("nickname"));
        model.addAttribute("profileImage", socialData.get("profileImage"));
        model.addAttribute("accessToken", socialData.get("accessToken"));

        return "client/social-signup";
    }

    @PostMapping("/signup")
    public String socialSignupProcess(@RequestParam String provider,
                                      @RequestParam String providerId,
                                      @RequestParam String accessToken,
                                      @RequestParam String userId,
                                      @RequestParam String userName,
                                      @RequestParam String email,
                                      @RequestParam(required = false) String gender,
                                      @RequestParam(required = false) String birth,
                                      @RequestParam(required = false) String profileImage,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        try {
            // 중복 체크
            if (!socialLoginService.isUserIdAvailable(userId)) {
                redirectAttributes.addFlashAttribute("error", "이미 사용 중인 아이디입니다.");
                return "redirect:/oauth/signup";
            }

            if (!socialLoginService.isEmailAvailable(email)) {
                redirectAttributes.addFlashAttribute("error", "이미 사용 중인 이메일입니다.");
                return "redirect:/oauth/signup";
            }

            // 회원가입 완료
            User user = socialLoginService.completeSocialSignup(
                    provider, providerId, accessToken,
                    userId, userName, email, gender, birth, profileImage
            );

            // 세션 정리 및 로그인 처리
            session.removeAttribute("socialSignup");
            session.setAttribute("loggedInUser", user.getUserId());
            session.setAttribute("userRole", user.getUserRole());
            session.setAttribute("loginTime", System.currentTimeMillis());
            session.setAttribute("socialLogin", provider);

            redirectAttributes.addFlashAttribute("message", "회원가입이 완료되었습니다. 환영합니다!");
            return "redirect:/";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "회원가입 처리 중 오류가 발생했습니다.");
            return "redirect:/oauth/signup";
        }
    }

    // ==================== 아이디/이메일 중복 체크 API ====================

    @GetMapping("/check-userid")
    @ResponseBody
    public Map<String, Object> checkUserId(@RequestParam String userId) {
        Map<String, Object> result = new HashMap<>();
        result.put("available", socialLoginService.isUserIdAvailable(userId));
        return result;
    }

    @GetMapping("/check-email")
    @ResponseBody
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> result = new HashMap<>();
        result.put("available", socialLoginService.isEmailAvailable(email));
        return result;
    }
}