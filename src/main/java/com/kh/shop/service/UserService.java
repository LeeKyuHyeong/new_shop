package com.kh.shop.service;

import com.kh.shop.dto.UserSearchDTO;
import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import com.kh.shop.util.LikeQueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Timing attack 방지용 더미 해시. 존재하지 않는 userId 로그인 시도에도 bcrypt 를 한 번 돌려
    // "사용자 없음" 과 "비밀번호 틀림" 의 응답 시간 차이를 제거한다.
    // 어떤 평문도 이 해시와 매치되지 않으므로 인증으로 통과될 위험은 없다.
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMye.IjPeMRoRgYnRsQHRH8YDXjf3Vw5Q9G";

    // 평문 비밀번호 sunset 날짜. 이 날짜 이후로 평문 분기는 자동 마이그레이션 대신 로그인을 거부한다.
    @Value("${bcrypt.sunset.date:2026-08-15}")
    private LocalDate bcryptSunsetDate;

    /**
     * 로그인 시도 결과. status 에 따라 호출자가 후처리한다.
     * - SUCCESS_BCRYPT:       정상 (이미 BCrypt)
     * - SUCCESS_PLAIN_MIGRATED: 평문이었으나 이번 로그인에서 BCrypt 로 자동 마이그레이션됨 — 사용자에게 경고 배너 표시 권장
     * - BLOCKED_PLAIN_SUNSET: 평문 사용자인데 sunset 날짜가 도래해 로그인 거부 — 비밀번호 찾기로 재설정 안내
     * - INVALID:              아이디/비밀번호 불일치
     */
    public record LoginResult(Status status, User user, LocalDate sunsetDate) {
        public enum Status { SUCCESS_BCRYPT, SUCCESS_PLAIN_MIGRATED, BLOCKED_PLAIN_SUNSET, INVALID }

        public boolean isSuccess() {
            return status == Status.SUCCESS_BCRYPT || status == Status.SUCCESS_PLAIN_MIGRATED;
        }
        public boolean wasPlainMigrated() {
            return status == Status.SUCCESS_PLAIN_MIGRATED;
        }
        public boolean isPlainSunsetBlocked() {
            return status == Status.BLOCKED_PLAIN_SUNSET;
        }
    }

    public boolean isDuplicateUserId(String userId) {
        return userRepository.findByUserId(userId).isPresent();
    }

    public boolean isDuplicateEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public User registerUser(String userId, String userPassword, String userName,
                             String email, String gender, LocalDate birth) {
        User user = User.builder()
                .userId(userId)
                .userPassword(passwordEncoder.encode(userPassword))
                .userName(userName)
                .email(email)
                .gender(gender)
                .birth(birth)
                .build();

        User savedUser = userRepository.save(user);

        // UserSetting 자동 생성
        createUserSetting(savedUser);

        return savedUser;
    }

    // UserSetting 생성 메서드
    private void createUserSetting(User user) {
        UserSetting setting = UserSetting.builder()
                .user(user)
                .theme("LIGHT")
                .language("KO")
                .notificationYn("Y")
                .emailReceiveYn("Y")
                .build();
        userSettingRepository.save(setting);
    }

    /**
     * 로그인 시도. 평문 비밀번호 마이그레이션 / sunset 차단을 포함한 전체 상태를 반환한다.
     * 단순 boolean 가 필요한 호출자는 {@link #loginUser(String, String)} 를 사용.
     */
    public LoginResult attemptLogin(String userId, String userPassword) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            // Timing parity: 존재하지 않는 userId 에도 bcrypt 한 번 수행하여 응답 시간 차이로
            // 계정 enumeration 되지 않도록 한다.
            passwordEncoder.matches(userPassword, DUMMY_BCRYPT_HASH);
            return new LoginResult(LoginResult.Status.INVALID, null, bcryptSunsetDate);
        }

        User user = userOpt.get();
        String storedPassword = user.getUserPassword();

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            // BCrypt 해시된 비밀번호 → BCrypt 검증
            if (passwordEncoder.matches(userPassword, storedPassword)) {
                return new LoginResult(LoginResult.Status.SUCCESS_BCRYPT, user, bcryptSunsetDate);
            }
            return new LoginResult(LoginResult.Status.INVALID, null, bcryptSunsetDate);
        }

        // 평문 비밀번호 (기존 유저) — sunset 정책 적용
        // TODO: countPlainPasswordUsers() 가 0 이 되면 이 분기와 DUMMY_BCRYPT_HASH 분기를 함께 제거.
        if (!storedPassword.equals(userPassword)) {
            return new LoginResult(LoginResult.Status.INVALID, null, bcryptSunsetDate);
        }

        // 비밀번호는 일치. sunset 날짜 도래 여부에 따라 마이그레이션 vs 차단
        if (bcryptSunsetDate != null && !LocalDate.now().isBefore(bcryptSunsetDate)) {
            log.warn("[BCRYPT-SUNSET] 평문 비밀번호 사용자가 sunset 이후 로그인 시도 - 차단. userId={}", userId);
            return new LoginResult(LoginResult.Status.BLOCKED_PLAIN_SUNSET, null, bcryptSunsetDate);
        }

        // sunset 전 → BCrypt 로 자동 마이그레이션
        user.setUserPassword(passwordEncoder.encode(userPassword));
        userRepository.save(user);
        log.warn("[BCRYPT-MIGRATION] 평문 비밀번호를 BCrypt 로 자동 마이그레이션. userId={}, sunsetDate={}",
                userId, bcryptSunsetDate);
        return new LoginResult(LoginResult.Status.SUCCESS_PLAIN_MIGRATED, user, bcryptSunsetDate);
    }

    /**
     * 단순 인증 결과만 필요한 경우의 어댑터. (예: 중복 로그인 체크)
     * Sunset 으로 차단된 경우도 empty 로 반환되므로 추가 분기가 필요한 호출자는 attemptLogin 을 사용할 것.
     */
    public Optional<User> loginUser(String userId, String userPassword) {
        LoginResult result = attemptLogin(userId, userPassword);
        return result.isSuccess() ? Optional.of(result.user()) : Optional.empty();
    }

    /**
     * 평문 비밀번호 sunset 날짜 (운영자 안내용).
     */
    public LocalDate getBcryptSunsetDate() {
        return bcryptSunsetDate;
    }

    /**
     * 평문 비밀번호로 남아있는 사용자 목록 (운영자 권고 메일 발송용).
     */
    @Transactional(readOnly = true)
    public List<User> findPlainPasswordUsers() {
        return userRepository.findPlainPasswordUsers();
    }

    // BCrypt 마이그레이션 진단 - 평문 비밀번호로 남아있는 사용자 수.
    // 운영자가 주기적으로 확인하여 0 에 가까워지면 평문 분기를 정리한다.
    @Transactional(readOnly = true)
    public long countPlainPasswordUsers() {
        return userRepository.countPlainPasswordUsers();
    }

    // 비밀번호 검증 (마이그레이션 없이 검증만)
    public Optional<User> verifyPassword(String userId, String rawPassword) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        String storedPassword = user.getUserPassword();

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            return passwordEncoder.matches(rawPassword, storedPassword) ? userOpt : Optional.empty();
        } else {
            return storedPassword.equals(rawPassword) ? userOpt : Optional.empty();
        }
    }

    // ==================== 관리자용 메서드 ====================

    // 전체 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedDateDesc();
    }

    // 조건 검색으로 사용자 조회. LIKE 와일드카드(%, _, \) 는 이스케이프하여 의도치 않은 광범위 매치 차단.
    public List<User> searchUsers(UserSearchDTO searchDTO) {
        if (searchDTO == null || !searchDTO.hasSearchCondition()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(
                LikeQueryUtil.escape(searchDTO.getUserId()),
                LikeQueryUtil.escape(searchDTO.getUserName()),
                LikeQueryUtil.escape(searchDTO.getEmail()),
                searchDTO.getGender(),
                searchDTO.getUserRole(),
                searchDTO.getUseYn(),
                searchDTO.getStartDate(),
                searchDTO.getEndDate()
        );
    }

    // 활성 사용자만 조회
    public List<User> getActiveUsers() {
        return userRepository.findByUseYnOrderByCreatedDateDesc("Y");
    }

    // 사용자 상세 조회
    public Optional<User> getUserByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    // 권한 변경
    @Transactional
    public User updateUserRole(String userId, String newRole) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUserRole(newRole);
            return userRepository.save(user);
        }
        return null;
    }

    // 사용자 상태 변경 (활성/비활성)
    @Transactional
    public User updateUserStatus(String userId, String useYn) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUseYn(useYn);
            return userRepository.save(user);
        }
        return null;
    }

    // 비밀번호 초기화
    @Transactional
    public User resetPassword(String userId, String newPassword) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setUserPassword(passwordEncoder.encode(newPassword));
            return userRepository.save(user);
        }
        return null;
    }
}