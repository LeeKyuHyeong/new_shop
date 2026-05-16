package com.kh.shop.controller.client;

import com.kh.shop.entity.User;
import com.kh.shop.service.SocialLoginService;
import com.kh.shop.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/oauth")
public class SocialLoginController {

    @Autowired
    private SocialLoginService socialLoginService;

    @Autowired
    private UserService userService;

    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("KAKAO", "NAVER", "GOOGLE");

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
            return cancelOrLoginError(session, redirectAttributes, "카카오 로그인이 취소되었습니다.");
        }

        // 마이페이지 연동 모드라면 로그인/회원가입 흐름 대신 linkOAuthByCode 호출
        if (isLinkMode(session, "KAKAO")) {
            return finishLink(session, redirectAttributes, "KAKAO", code, null);
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
            return cancelOrLoginError(session, redirectAttributes, "네이버 로그인이 취소되었습니다.");
        }

        // state 검증
        String savedState = (String) session.getAttribute("naverState");
        if (savedState == null || !savedState.equals(state)) {
            // 연동 모드의 state 검증 실패도 같은 메시지로
            if (isLinkMode(session, "NAVER")) {
                session.removeAttribute("socialLinkMode");
                redirectAttributes.addFlashAttribute("settingError", "잘못된 요청입니다.");
                return "redirect:/mypage/setting";
            }
            redirectAttributes.addFlashAttribute("loginError", "잘못된 요청입니다.");
            return "redirect:/login";
        }
        session.removeAttribute("naverState");

        if (isLinkMode(session, "NAVER")) {
            return finishLink(session, redirectAttributes, "NAVER", code, state);
        }

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
            return cancelOrLoginError(session, redirectAttributes, "구글 로그인이 취소되었습니다.");
        }

        if (isLinkMode(session, "GOOGLE")) {
            return finishLink(session, redirectAttributes, "GOOGLE", code, null);
        }

        Map<String, Object> result = socialLoginService.googleLogin(code);
        return handleOAuthResult(result, "GOOGLE", session, redirectAttributes);
    }

    // ==================== 마이페이지 연동 시작 ====================

    /**
     * 마이페이지에서 소셜 계정 연동 시작.
     * 로그인된 본인이 명시적으로 호출하는 경로. 세션에 link mode 마킹 후 OAuth 진입.
     * 콜백은 기존 callback 핸들러를 재사용한다 (콜백 URL 재설정 비용 회피).
     */
    @GetMapping("/link/{provider}")
    public String startLink(@PathVariable String provider,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login";
        }

        String upper = provider == null ? "" : provider.toUpperCase();
        if (!SUPPORTED_PROVIDERS.contains(upper)) {
            redirectAttributes.addFlashAttribute("settingError", "지원하지 않는 소셜 제공자입니다.");
            return "redirect:/mypage/setting";
        }

        // 이미 연동되어 있으면 진입하지 않음
        if (socialLoginService.getLinkedAccountsByProvider(userId).containsKey(upper)) {
            redirectAttributes.addFlashAttribute("settingError", "이미 " + providerLabel(upper) + " 계정이 연동되어 있습니다.");
            return "redirect:/mypage/setting";
        }

        // 콜백 핸들러가 link 모드를 인지하도록 세션 플래그 설정 + 사용자 고정
        session.setAttribute("socialLinkMode", upper);
        session.setAttribute("socialLinkUserId", userId);

        switch (upper) {
            case "KAKAO":
                return "redirect:" + socialLoginService.getKakaoAuthUrl();
            case "NAVER":
                String state = UUID.randomUUID().toString();
                session.setAttribute("naverState", state);
                return "redirect:" + socialLoginService.getNaverAuthUrl(state);
            case "GOOGLE":
                return "redirect:" + socialLoginService.getGoogleAuthUrl();
            default:
                return "redirect:/mypage/setting";
        }
    }

    // ==================== 공통 처리 ====================

    private boolean isLinkMode(HttpSession session, String provider) {
        Object mode = session.getAttribute("socialLinkMode");
        return mode != null && provider.equals(mode.toString());
    }

    private String cancelOrLoginError(HttpSession session, RedirectAttributes redirectAttributes, String message) {
        if (session.getAttribute("socialLinkMode") != null) {
            session.removeAttribute("socialLinkMode");
            session.removeAttribute("socialLinkUserId");
            redirectAttributes.addFlashAttribute("settingError", message);
            return "redirect:/mypage/setting";
        }
        redirectAttributes.addFlashAttribute("loginError", message);
        return "redirect:/login";
    }

    private String finishLink(HttpSession session, RedirectAttributes redirectAttributes,
                              String provider, String code, String state) {
        // 세션의 현재 사용자와 link 시작 시점 사용자가 동일해야 함 (세션 탈취/사용자 전환 방어)
        String currentUserId = (String) session.getAttribute("loggedInUser");
        Object linkUserIdObj = session.getAttribute("socialLinkUserId");
        session.removeAttribute("socialLinkMode");
        session.removeAttribute("socialLinkUserId");

        if (currentUserId == null || linkUserIdObj == null || !currentUserId.equals(linkUserIdObj.toString())) {
            redirectAttributes.addFlashAttribute("settingError", "세션이 만료되었습니다. 다시 시도해주세요.");
            return "redirect:/mypage/setting";
        }

        Optional<User> userOpt = userService.getUserByUserId(currentUserId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("settingError", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/mypage/setting";
        }

        try {
            socialLoginService.linkOAuthByCode(userOpt.get(), provider, code, state);
            redirectAttributes.addFlashAttribute("settingMessage", providerLabel(provider) + " 계정이 연동되었습니다.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("settingError", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("settingError", providerLabel(provider) + " 연동 처리 중 오류가 발생했습니다.");
        }
        return "redirect:/mypage/setting";
    }

    private String providerLabel(String provider) {
        switch (provider) {
            case "KAKAO":  return "카카오";
            case "NAVER":  return "네이버";
            case "GOOGLE": return "구글";
            default:       return provider;
        }
    }

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

        return "redirect:/";
    }

    // ==================== 소셜 회원가입 ====================

    @GetMapping("/signup")
    public String socialSignupForm(HttpSession session, Model model) {
        Map<String, Object> socialData = (Map<String, Object>) session.getAttribute("socialSignup");

        if (socialData == null) {
            return "redirect:/login";
        }

        // accessToken 은 더 이상 form 으로 노출하지 않는다 (XSS exfiltration 위험 + 어차피 저장 안 함)
        model.addAttribute("provider", socialData.get("provider"));
        model.addAttribute("providerId", socialData.get("providerId"));
        model.addAttribute("email", socialData.get("email"));
        model.addAttribute("nickname", socialData.get("nickname"));
        model.addAttribute("profileImage", socialData.get("profileImage"));

        return "client/social-signup";
    }

    @PostMapping("/signup")
    public String socialSignupProcess(@RequestParam String provider,
                                      @RequestParam String providerId,
                                      @RequestParam String userId,
                                      @RequestParam String userName,
                                      @RequestParam String email,
                                      @RequestParam(required = false) String gender,
                                      @RequestParam(required = false) String birth,
                                      @RequestParam(required = false) String profileImage,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {

        // 세션의 socialSignup 데이터가 있어야 한다 (정상 OAuth 콜백 후의 흐름만 허용).
        // 또한 클라이언트가 보낸 provider/providerId 가 서버 세션의 값과 일치해야 한다 -
        // 이를 통해 임의의 (provider, providerId) 로 가짜 가입을 차단.
        Map<String, Object> socialData = (Map<String, Object>) session.getAttribute("socialSignup");
        if (socialData == null
                || !provider.equals(socialData.get("provider"))
                || !providerId.equals(socialData.get("providerId"))) {
            redirectAttributes.addFlashAttribute("error", "잘못된 접근입니다. 다시 로그인해주세요.");
            return "redirect:/login";
        }

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

            // 회원가입 완료 (accessToken 저장 안 함)
            User user = socialLoginService.completeSocialSignup(
                    provider, providerId,
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