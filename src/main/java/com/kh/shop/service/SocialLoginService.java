package com.kh.shop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.shop.config.SocialLoginConfig;
import com.kh.shop.entity.SocialAccount;
import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.SocialAccountRepository;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SocialLoginService {

    @Autowired
    private SocialLoginConfig socialLoginConfig;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ==================== 카카오 ====================

    public String getKakaoAuthUrl() {
        return "https://kauth.kakao.com/oauth/authorize" +
                "?client_id=" + socialLoginConfig.getKakaoClientId() +
                "&redirect_uri=" + socialLoginConfig.getKakaoRedirectUri() +
                "&response_type=code";
    }

    public Map<String, Object> kakaoLogin(String code) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> info = fetchKakaoUserInfo(code);
            return processOAuthLogin("KAKAO",
                    info.get("providerId"), info.get("email"),
                    info.get("nickname"), info.get("profileImage"),
                    info.get("accessToken"));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        return result;
    }

    private Map<String, String> fetchKakaoUserInfo(String code) throws Exception {
        // 1. 액세스 토큰 받기
        String tokenUrl = "https://kauth.kakao.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", socialLoginConfig.getKakaoClientId());
        params.add("redirect_uri", socialLoginConfig.getKakaoRedirectUri());
        params.add("code", code);
        if (socialLoginConfig.getKakaoClientSecret() != null && !socialLoginConfig.getKakaoClientSecret().isEmpty()) {
            params.add("client_secret", socialLoginConfig.getKakaoClientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, String.class);

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 2. 사용자 정보 가져오기
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, String.class);

        JsonNode userJson = objectMapper.readTree(userResponse.getBody());

        String providerId = userJson.get("id").asText();
        String nickname = "";
        String email = "";
        String profileImage = "";

        if (userJson.has("kakao_account")) {
            JsonNode account = userJson.get("kakao_account");
            if (account.has("email")) {
                email = account.get("email").asText();
            }
            if (account.has("profile")) {
                JsonNode profile = account.get("profile");
                if (profile.has("nickname")) {
                    nickname = profile.get("nickname").asText();
                }
                if (profile.has("profile_image_url")) {
                    profileImage = profile.get("profile_image_url").asText();
                }
            }
        }

        Map<String, String> info = new HashMap<>();
        info.put("providerId", providerId);
        info.put("email", email);
        info.put("nickname", nickname);
        info.put("profileImage", profileImage);
        info.put("accessToken", accessToken);
        return info;
    }

    // ==================== 네이버 ====================

    public String getNaverAuthUrl(String state) {
        return "https://nid.naver.com/oauth2.0/authorize" +
                "?client_id=" + socialLoginConfig.getNaverClientId() +
                "&redirect_uri=" + socialLoginConfig.getNaverRedirectUri() +
                "&response_type=code" +
                "&state=" + state;
    }

    public Map<String, Object> naverLogin(String code, String state) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> info = fetchNaverUserInfo(code, state);
            return processOAuthLogin("NAVER",
                    info.get("providerId"), info.get("email"),
                    info.get("nickname"), info.get("profileImage"),
                    info.get("accessToken"));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "네이버 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        return result;
    }

    private Map<String, String> fetchNaverUserInfo(String code, String state) throws Exception {
        // 1. 액세스 토큰 받기
        String tokenUrl = "https://nid.naver.com/oauth2.0/token" +
                "?grant_type=authorization_code" +
                "&client_id=" + socialLoginConfig.getNaverClientId() +
                "&client_secret=" + socialLoginConfig.getNaverClientSecret() +
                "&code=" + code +
                "&state=" + state;

        ResponseEntity<String> tokenResponse = restTemplate.getForEntity(tokenUrl, String.class);
        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 2. 사용자 정보 가져오기
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<String> userRequest = new HttpEntity<>(headers);
        ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, String.class);

        JsonNode userJson = objectMapper.readTree(userResponse.getBody());
        JsonNode response = userJson.get("response");

        String providerId = response.get("id").asText();
        String nickname = response.has("nickname") ? response.get("nickname").asText() : "";
        String email = response.has("email") ? response.get("email").asText() : "";
        String profileImage = response.has("profile_image") ? response.get("profile_image").asText() : "";

        Map<String, String> info = new HashMap<>();
        info.put("providerId", providerId);
        info.put("email", email);
        info.put("nickname", nickname);
        info.put("profileImage", profileImage);
        info.put("accessToken", accessToken);
        return info;
    }

    // ==================== 구글 ====================

    public String getGoogleAuthUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth" +
                "?client_id=" + socialLoginConfig.getGoogleClientId() +
                "&redirect_uri=" + socialLoginConfig.getGoogleRedirectUri() +
                "&response_type=code" +
                "&scope=email%20profile";
    }

    public Map<String, Object> googleLogin(String code) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, String> info = fetchGoogleUserInfo(code);
            return processOAuthLogin("GOOGLE",
                    info.get("providerId"), info.get("email"),
                    info.get("nickname"), info.get("profileImage"),
                    info.get("accessToken"));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "구글 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
        return result;
    }

    private Map<String, String> fetchGoogleUserInfo(String code) throws Exception {
        // 1. 액세스 토큰 받기
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", socialLoginConfig.getGoogleClientId());
        params.add("client_secret", socialLoginConfig.getGoogleClientSecret());
        params.add("redirect_uri", socialLoginConfig.getGoogleRedirectUri());
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
        ResponseEntity<String> tokenResponse = restTemplate.postForEntity(tokenUrl, tokenRequest, String.class);

        JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
        String accessToken = tokenJson.get("access_token").asText();

        // 2. 사용자 정보 가져오기
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);

        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<String> userResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userRequest, String.class);

        JsonNode userJson = objectMapper.readTree(userResponse.getBody());

        String providerId = userJson.get("id").asText();
        String email = userJson.has("email") ? userJson.get("email").asText() : "";
        String nickname = userJson.has("name") ? userJson.get("name").asText() : "";
        String profileImage = userJson.has("picture") ? userJson.get("picture").asText() : "";

        Map<String, String> info = new HashMap<>();
        info.put("providerId", providerId);
        info.put("email", email);
        info.put("nickname", nickname);
        info.put("profileImage", profileImage);
        info.put("accessToken", accessToken);
        return info;
    }

    // ==================== 공통 처리 ====================

    /**
     * 소셜 로그인 처리 - 기존 소셜 계정이면 로그인, 신규면 회원가입 페이지로 이동.
     *
     * 보안:
     * - accessToken 은 DB 에 저장하지 않는다 (코드 어디서도 사용하지 않으므로 dead data + 유출 위험).
     *   향후 API 호출 시 필요해지면 AES 암호화 후 저장할 것.
     * - 이메일 기반 자동 계정 연동은 차단한다. 공격자가 victim 이메일로 소셜 계정을 만들면
     *   기존 회원 계정을 탈취할 수 있음 (Account Takeover). 기존 회원이 있으면 로그인 후
     *   설정에서 직접 연동하도록 안내한다.
     */
    public Map<String, Object> processOAuthLogin(String provider, String providerId, String email, String nickname, String profileImage, String accessToken) {
        Map<String, Object> result = new HashMap<>();

        // 1. 기존 소셜 계정 확인 (providerId 매치 - 안전한 식별자)
        Optional<SocialAccount> existingSocial = socialAccountRepository.findByProviderAndProviderId(provider, providerId);

        if (existingSocial.isPresent()) {
            // 기존 소셜 계정이 있으면 로그인 처리. accessToken 은 저장하지 않음.
            SocialAccount social = existingSocial.get();
            social.setSocialName(nickname);
            social.setProfileImage(profileImage);
            socialAccountRepository.save(social);

            result.put("success", true);
            result.put("isNewUser", false);
            result.put("user", social.getUser());
            return result;
        }

        // 2. 이메일이 기존 회원과 겹치는지 확인. 겹치면 자동 연동을 거부하고 명시적 로그인 유도.
        if (email != null && !email.isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                result.put("success", false);
                result.put("message", "이 이메일(" + email + ")로 이미 가입된 계정이 있습니다. "
                        + "기존 아이디/비밀번호로 로그인 후 마이페이지에서 " + provider + " 계정을 연동해주세요.");
                return result;
            }
        }

        // 3. 신규 회원 - 회원가입 페이지로 이동 필요. accessToken 도 흘려보내지 않음
        //    (signup 시점에 다시 OAuth code 교환을 하지 않으므로 providerId 기반으로만 연결).
        result.put("success", true);
        result.put("isNewUser", true);
        result.put("provider", provider);
        result.put("providerId", providerId);
        result.put("email", email);
        result.put("nickname", nickname);
        result.put("profileImage", profileImage);

        return result;
    }

    /**
     * 소셜 회원가입 완료. accessToken 은 DB 에 저장하지 않는다 (보안 - processOAuthLogin 주석 참조).
     */
    @Transactional
    public User completeSocialSignup(String provider, String providerId,
                                     String userId, String userName, String email,
                                     String gender, String birth, String profileImage) {

        // 1. User 생성
        User user = User.builder()
                .userId(userId)
                .userPassword(passwordEncoder.encode(UUID.randomUUID().toString())) // 랜덤 비밀번호 암호화
                .userName(userName)
                .email(email)
                .gender(gender)
                .build();

        if (birth != null && !birth.isEmpty()) {
            try {
                user.setBirth(java.time.LocalDate.parse(birth));
            } catch (Exception e) {
                // 날짜 파싱 실패 시 무시
            }
        }

        user = userRepository.save(user);

        // 2. UserSetting 생성
        UserSetting setting = UserSetting.builder()
                .user(user)
                .theme("LIGHT")
                .language("KO")
                .notificationYn("Y")
                .emailReceiveYn("Y")
                .build();
        userSettingRepository.save(setting);

        // 3. SocialAccount 연결 (accessToken 저장 안 함)
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .socialEmail(email)
                .socialName(userName)
                .profileImage(profileImage)
                .build();

        socialAccountRepository.save(socialAccount);

        return user;
    }

    /**
     * 인증된 사용자 본인이 소셜 계정을 명시적으로 연동 (마이페이지 등에서 호출).
     * processOAuthLogin 의 자동 연동을 대체하는 안전한 경로.
     * 호출 측에서 user 가 현재 세션의 본인임을 반드시 검증해야 한다.
     */
    @Transactional
    public void linkSocialAccount(User user, String provider, String providerId,
                                  String email, String nickname, String profileImage) {
        // 동일 provider 의 동일 providerId 가 다른 user 에 묶여있으면 거부
        socialAccountRepository.findByProviderAndProviderId(provider, providerId).ifPresent(existing -> {
            if (!existing.getUser().getUserId().equals(user.getUserId())) {
                throw new IllegalStateException("해당 소셜 계정은 이미 다른 회원에 연동되어 있습니다.");
            }
        });

        // 본인이 이미 같은 provider 로 연동되어 있으면 중복 저장 방지
        if (socialAccountRepository.findByUser_UserIdAndProvider(user.getUserId(), provider).isPresent()) {
            throw new IllegalStateException("이미 " + provider + " 계정이 연동되어 있습니다.");
        }

        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .socialEmail(email)
                .socialName(nickname)
                .profileImage(profileImage)
                .build();

        socialAccountRepository.save(socialAccount);
    }

    /**
     * OAuth 콜백에서 받은 code 로 소셜 정보를 가져온 뒤 현재 로그인 사용자에 연동.
     * 자동 로그인/회원가입 분기 없이 연동만 수행한다. providerId 기준으로 매칭하며
     * 이메일 충돌은 무시 (본인이 명시적으로 진행하는 흐름이므로 ATO 위험 없음).
     */
    @Transactional
    public void linkOAuthByCode(User user, String provider, String code, String state) throws Exception {
        Map<String, String> info;
        switch (provider) {
            case "KAKAO":
                info = fetchKakaoUserInfo(code);
                break;
            case "NAVER":
                info = fetchNaverUserInfo(code, state);
                break;
            case "GOOGLE":
                info = fetchGoogleUserInfo(code);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 소셜 제공자: " + provider);
        }
        linkSocialAccount(user, provider,
                info.get("providerId"), info.get("email"),
                info.get("nickname"), info.get("profileImage"));
    }

    /**
     * 현재 사용자의 (provider) 소셜 연동을 해제.
     * 호출 측에서 비밀번호 확인 등 본인 검증을 반드시 선행할 것.
     */
    @Transactional
    public void unlinkSocialAccount(String userId, String provider) {
        SocialAccount account = socialAccountRepository.findByUser_UserIdAndProvider(userId, provider)
                .orElseThrow(() -> new IllegalStateException("연동된 " + provider + " 계정이 없습니다."));
        socialAccountRepository.delete(account);
    }

    /**
     * 사용자의 연동된 소셜 계정 목록 (provider → SocialAccount 매핑).
     */
    public Map<String, SocialAccount> getLinkedAccountsByProvider(String userId) {
        Map<String, SocialAccount> result = new HashMap<>();
        for (SocialAccount account : socialAccountRepository.findByUser_UserId(userId)) {
            result.put(account.getProvider(), account);
        }
        return result;
    }

    /**
     * 아이디 중복 체크
     */
    public boolean isUserIdAvailable(String userId) {
        return !userRepository.findByUserId(userId).isPresent();
    }

    /**
     * 이메일 중복 체크
     */
    public boolean isEmailAvailable(String email) {
        return !userRepository.findByEmail(email).isPresent();
    }
}