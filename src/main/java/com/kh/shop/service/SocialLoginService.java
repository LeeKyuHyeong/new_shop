package com.kh.shop.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kh.shop.config.SocialLoginConfig;
import com.kh.shop.entity.SocialAccount;
import com.kh.shop.entity.User;
import com.kh.shop.repository.SocialAccountRepository;
import com.kh.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

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

            // 3. 사용자 처리
            return processOAuthLogin("KAKAO", providerId, email, nickname, profileImage, accessToken);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
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

            // 3. 사용자 처리
            return processOAuthLogin("NAVER", providerId, email, nickname, profileImage, accessToken);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "네이버 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
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

            // 3. 사용자 처리
            return processOAuthLogin("GOOGLE", providerId, email, nickname, profileImage, accessToken);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "구글 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }

        return result;
    }

    // ==================== 공통 처리 ====================

    /**
     * 소셜 로그인 처리 - 기존 회원이면 로그인, 신규면 회원가입 페이지로 이동
     */
    public Map<String, Object> processOAuthLogin(String provider, String providerId, String email, String nickname, String profileImage, String accessToken) {
        Map<String, Object> result = new HashMap<>();

        // 1. 기존 소셜 계정 확인
        Optional<SocialAccount> existingSocial = socialAccountRepository.findByProviderAndProviderId(provider, providerId);

        if (existingSocial.isPresent()) {
            // 기존 소셜 계정이 있으면 로그인 처리
            SocialAccount social = existingSocial.get();
            social.setAccessToken(accessToken);
            social.setSocialName(nickname);
            social.setProfileImage(profileImage);
            socialAccountRepository.save(social);

            result.put("success", true);
            result.put("isNewUser", false);
            result.put("user", social.getUser());
            return result;
        }

        // 2. 이메일로 기존 회원 확인 (소셜 계정 연동 유도)
        if (email != null && !email.isEmpty()) {
            Optional<User> existingUser = userRepository.findByEmail(email);
            if (existingUser.isPresent()) {
                // 기존 회원이 있으면 소셜 계정 연동
                User user = existingUser.get();
                linkSocialAccount(user, provider, providerId, email, nickname, profileImage, accessToken);

                result.put("success", true);
                result.put("isNewUser", false);
                result.put("user", user);
                result.put("linked", true); // 연동되었음을 표시
                return result;
            }
        }

        // 3. 신규 회원 - 회원가입 페이지로 이동 필요
        result.put("success", true);
        result.put("isNewUser", true);
        result.put("provider", provider);
        result.put("providerId", providerId);
        result.put("email", email);
        result.put("nickname", nickname);
        result.put("profileImage", profileImage);
        result.put("accessToken", accessToken);

        return result;
    }

    /**
     * 소셜 회원가입 완료
     */
    @Transactional
    public User completeSocialSignup(String provider, String providerId, String accessToken,
                                     String userId, String userName, String email,
                                     String gender, String birth, String profileImage) {

        // 1. User 생성
        User user = User.builder()
                .userId(userId)
                .userPassword(UUID.randomUUID().toString()) // 랜덤 비밀번호 (소셜 로그인이므로 사용 안함)
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

        // 2. SocialAccount 연결
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .socialEmail(email)
                .socialName(userName)
                .profileImage(profileImage)
                .accessToken(accessToken)
                .build();

        socialAccountRepository.save(socialAccount);

        return user;
    }

    /**
     * 기존 회원에 소셜 계정 연동
     */
    @Transactional
    public void linkSocialAccount(User user, String provider, String providerId,
                                  String email, String nickname, String profileImage, String accessToken) {
        SocialAccount socialAccount = SocialAccount.builder()
                .user(user)
                .provider(provider)
                .providerId(providerId)
                .socialEmail(email)
                .socialName(nickname)
                .profileImage(profileImage)
                .accessToken(accessToken)
                .build();

        socialAccountRepository.save(socialAccount);
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