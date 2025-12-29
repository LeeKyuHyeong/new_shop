package com.kh.shop.service;

import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

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
                .userPassword(userPassword)
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
        Optional<User> user = userRepository.findByUserId(userId);
        if (user.isPresent() && user.get().getUserPassword().equals(userPassword)) {
            return user;
        }
        return Optional.empty();
    }

    // ==================== 관리자용 메서드 ====================

    // 전체 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAllByOrderByCreatedDateDesc();
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
            user.setUserPassword(newPassword);
            return userRepository.save(user);
        }
        return null;
    }
}