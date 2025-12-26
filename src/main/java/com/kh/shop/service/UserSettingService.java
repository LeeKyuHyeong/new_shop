package com.kh.shop.service;

import com.kh.shop.entity.User;
import com.kh.shop.entity.UserSetting;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.UserSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserSettingService {

    @Autowired
    private UserSettingRepository userSettingRepository;

    @Autowired
    private UserRepository userRepository;

    // 사용자 설정 조회 (없으면 기본값으로 생성)
    @Transactional
    public UserSetting getOrCreateSetting(String userId) {
        Optional<UserSetting> settingOpt = userSettingRepository.findByUser_UserId(userId);

        if (settingOpt.isPresent()) {
            return settingOpt.get();
        }

        // 없으면 새로 생성
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return null;
        }

        UserSetting newSetting = UserSetting.builder()
                .user(userOpt.get())
                .theme("LIGHT")
                .language("KO")
                .notificationYn("Y")
                .emailReceiveYn("Y")
                .build();

        return userSettingRepository.save(newSetting);
    }

    // 사용자 설정 조회 (없으면 null)
    public Optional<UserSetting> getSetting(String userId) {
        return userSettingRepository.findByUser_UserId(userId);
    }

    // 테마 설정 업데이트
    @Transactional
    public UserSetting updateTheme(String userId, String theme) {
        UserSetting setting = getOrCreateSetting(userId);
        if (setting != null) {
            setting.setTheme(theme);
            return userSettingRepository.save(setting);
        }
        return null;
    }

    // 알림 설정 업데이트
    @Transactional
    public UserSetting updateNotification(String userId, String notificationYn) {
        UserSetting setting = getOrCreateSetting(userId);
        if (setting != null) {
            setting.setNotificationYn(notificationYn);
            return userSettingRepository.save(setting);
        }
        return null;
    }

    // 이메일 수신 설정 업데이트
    @Transactional
    public UserSetting updateEmailReceive(String userId, String emailReceiveYn) {
        UserSetting setting = getOrCreateSetting(userId);
        if (setting != null) {
            setting.setEmailReceiveYn(emailReceiveYn);
            return userSettingRepository.save(setting);
        }
        return null;
    }

    // 전체 설정 업데이트
    @Transactional
    public UserSetting updateAllSettings(String userId, String theme, String notificationYn, String emailReceiveYn) {
        UserSetting setting = getOrCreateSetting(userId);
        if (setting != null) {
            if (theme != null) setting.setTheme(theme);
            if (notificationYn != null) setting.setNotificationYn(notificationYn);
            if (emailReceiveYn != null) setting.setEmailReceiveYn(emailReceiveYn);
            return userSettingRepository.save(setting);
        }
        return null;
    }

    // 테마만 조회 (비로그인 시 기본값 반환)
    public String getTheme(String userId) {
        if (userId == null) {
            return "LIGHT";
        }
        Optional<UserSetting> settingOpt = userSettingRepository.findByUser_UserId(userId);
        return settingOpt.map(UserSetting::getTheme).orElse("LIGHT");
    }
}