package com.kh.shop.service;

import com.kh.shop.dto.UserSearchDTO;
import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import com.kh.shop.util.LikeQueryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Optional<User> loginUser(String userId, String userPassword) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            // Timing parity: 존재하지 않는 userId 에도 bcrypt 한 번 수행하여 응답 시간 차이로
            // 계정 enumeration 되지 않도록 한다.
            passwordEncoder.matches(userPassword, DUMMY_BCRYPT_HASH);
            return Optional.empty();
        }

        User user = userOpt.get();
        String storedPassword = user.getUserPassword();

        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            // BCrypt 해시된 비밀번호 → BCrypt 검증
            if (passwordEncoder.matches(userPassword, storedPassword)) {
                return userOpt;
            }
        } else {
            // 평문 비밀번호 (기존 유저) → 평문 비교 후 BCrypt로 마이그레이션.
            // TODO: countPlainPasswordUsers() 가 0 이 되면 이 분기와 DUMMY_BCRYPT_HASH 분기를 함께 제거.
            //       남은 사용자는 비밀번호 재설정으로 강제 마이그레이션 권장.
            if (storedPassword.equals(userPassword)) {
                user.setUserPassword(passwordEncoder.encode(userPassword));
                userRepository.save(user);
                log.warn("[BCRYPT-MIGRATION] 평문 비밀번호를 BCrypt 로 자동 마이그레이션. userId={}", userId);
                return userOpt;
            }
        }

        return Optional.empty();
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