package com.kh.shop.service;

import com.kh.shop.entity.SiteSetting;
import com.kh.shop.repository.SiteSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SiteSettingService {

    @Autowired
    private SiteSettingRepository siteSettingRepository;

    // 설정 키 상수
    public static final String KEY_SLIDE_DURATION = "SLIDE_DURATION";
    public static final String KEY_SITE_NAME = "SITE_NAME";
    public static final String KEY_SITE_LOGO = "SITE_LOGO";
    public static final String KEY_POPUP_DURATION = "POPUP_DURATION";

    // 전체 설정 조회
    public List<SiteSetting> getAllSettings() {
        return siteSettingRepository.findAll();
    }

    // 특정 설정 조회
    public Optional<SiteSetting> getSetting(String key) {
        return siteSettingRepository.findById(key);
    }

    // 설정 값만 조회 (기본값 지원)
    public String getSettingValue(String key, String defaultValue) {
        return siteSettingRepository.findById(key)
                .map(SiteSetting::getSettingValue)
                .orElse(defaultValue);
    }

    // 슬라이드 지속시간 조회 (기본 5초)
    public int getSlideDuration() {
        String value = getSettingValue(KEY_SLIDE_DURATION, "5");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    // 팝업 지속시간 조회 (기본 1일)
    public int getPopupDuration() {
        String value = getSettingValue(KEY_POPUP_DURATION, "1");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    // 설정 저장/수정
    @Transactional
    public SiteSetting saveSetting(String key, String value, String description) {
        SiteSetting setting = siteSettingRepository.findById(key)
                .orElse(SiteSetting.builder().settingKey(key).build());

        setting.setSettingValue(value);
        if (description != null) {
            setting.setSettingDescription(description);
        }

        return siteSettingRepository.save(setting);
    }

    // 여러 설정 일괄 저장
    @Transactional
    public void saveSettings(List<SiteSetting> settings) {
        for (SiteSetting setting : settings) {
            saveSetting(setting.getSettingKey(), setting.getSettingValue(), setting.getSettingDescription());
        }
    }

    // 초기 설정 데이터 생성
    @Transactional
    public void initDefaultSettings() {
        if (!siteSettingRepository.existsById(KEY_SLIDE_DURATION)) {
            saveSetting(KEY_SLIDE_DURATION, "5", "슬라이드 지속시간(초)");
        }
        if (!siteSettingRepository.existsById(KEY_SITE_NAME)) {
            saveSetting(KEY_SITE_NAME, "KH SHOP", "사이트명");
            if (!siteSettingRepository.existsById(KEY_POPUP_DURATION)) {
                saveSetting(KEY_POPUP_DURATION, "1", "팝업 지속시간(일) - 오늘하루보지않기");
            }
        }
    }
}