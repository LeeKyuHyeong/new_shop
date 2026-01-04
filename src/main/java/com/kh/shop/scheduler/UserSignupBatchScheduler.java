package com.kh.shop.scheduler;

import com.kh.shop.config.SchedulerConfig;
import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSignupBatchScheduler {

    private final UserRepository userRepository;
    private final UserSettingRepository userSettingRepository;
    private final Random random = new Random();

    // 성씨 데이터
    private final String[] LAST_NAMES = {
            "김", "이", "박", "최", "정", "강", "조", "윤", "장", "임",
            "한", "오", "서", "신", "권", "황", "안", "송", "류", "홍"
    };

    // 이름 데이터 (2글자)
    private final String[] FIRST_NAMES = {
            "민준", "서준", "예준", "도윤", "시우", "주원", "하준", "지호", "지후", "준서",
            "서연", "서윤", "지우", "서현", "민서", "하은", "하윤", "윤서", "지민", "채원",
            "수빈", "지훈", "현우", "준혁", "승민", "유진", "소희", "은지", "예진", "다은",
            "태현", "민재", "성민", "재원", "동현", "수현", "지원", "예림", "수아", "다인"
    };

    // 이메일 도메인
    private final String[] EMAIL_DOMAINS = {
            "gmail.com", "naver.com", "daum.net", "kakao.com", "hanmail.net"
    };

    // 아이디 접두사
    private final String[] ID_PREFIXES = {
            "user", "member", "happy", "cool", "nice", "smart", "lucky", "super", "best", "top"
    };

    /**
     * 매일 5시에 랜덤 회원 1명 가입
     */
    @Scheduled(cron = "0 0 5 * * *")
    @Transactional
    public void createRandomUser() {
        log.info("========== [배치] 랜덤 회원가입 시작 ==========");

        try {
            // 1. 유니크한 아이디 생성
            String userId = generateUniqueUserId();

            // 2. 회원 정보 생성
            String userName = generateUserName();
            String email = generateUniqueEmail(userId);
            String password = "test1234!";  // 테스트용 기본 비밀번호
            String gender = random.nextBoolean() ? "M" : "F";
            LocalDate birth = generateBirthDate();

            // 3. 회원 생성
            User user = User.builder()
                    .userId(userId)
                    .userPassword(password)
                    .userName(userName)
                    .email(email)
                    .gender(gender)
                    .birth(birth)
                    .build();

            User savedUser = userRepository.save(user);

            // 4. 회원 설정 생성
            UserSetting setting = UserSetting.builder()
                    .user(savedUser)
                    .theme(random.nextBoolean() ? "LIGHT" : "DARK")
                    .language("KO")
                    .notificationYn(random.nextBoolean() ? "Y" : "N")
                    .emailReceiveYn(random.nextBoolean() ? "Y" : "N")
                    .build();

            userSettingRepository.save(setting);

            log.info("[배치] 회원가입 완료 - ID: {}, 이름: {}, 이메일: {}, 성별: {}, 생년월일: {}",
                    savedUser.getUserId(),
                    savedUser.getUserName(),
                    savedUser.getEmail(),
                    gender.equals("M") ? "남성" : "여성",
                    birth);

        } catch (Exception e) {
            log.error("[배치] 회원가입 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 랜덤 회원가입 종료 ==========");
    }

    /**
     * 유니크한 아이디 생성
     */
    private String generateUniqueUserId() {
        String userId;
        int attempts = 0;
        do {
            String prefix = ID_PREFIXES[random.nextInt(ID_PREFIXES.length)];
            int number = 1000 + random.nextInt(9000);
            userId = prefix + number;
            attempts++;
        } while (userRepository.findByUserId(userId).isPresent() && attempts < 100);

        return userId;
    }

    /**
     * 랜덤 이름 생성
     */
    private String generateUserName() {
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        return lastName + firstName;
    }

    /**
     * 유니크한 이메일 생성
     */
    private String generateUniqueEmail(String userId) {
        String domain = EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
        return userId + "@" + domain;
    }

    /**
     * 랜덤 생년월일 생성 (1980~2005년)
     */
    private LocalDate generateBirthDate() {
        int year = 1980 + random.nextInt(26);  // 1980 ~ 2005
        int month = 1 + random.nextInt(12);    // 1 ~ 12
        int maxDay = LocalDate.of(year, month, 1).lengthOfMonth();
        int day = 1 + random.nextInt(maxDay);  // 1 ~ 해당월 마지막일

        return LocalDate.of(year, month, day);
    }
}