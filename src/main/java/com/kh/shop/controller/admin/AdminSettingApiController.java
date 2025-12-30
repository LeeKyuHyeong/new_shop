package com.kh.shop.controller.admin;

import com.kh.shop.service.SiteSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/setting")
public class AdminSettingApiController {

    @Autowired
    private SiteSettingService siteSettingService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/save")
    public Map<String, Object> saveSettings(
            @RequestParam(required = false) String siteName,
            @RequestParam(required = false) String slideDuration,
            @RequestParam(required = false) String popupDuration,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        try {
            if (siteName != null) {
                siteSettingService.saveSetting(
                        SiteSettingService.KEY_SITE_NAME, siteName, "사이트명");
            }

            if (slideDuration != null) {
                // 숫자 검증
                int duration = Integer.parseInt(slideDuration);
                if (duration < 1 || duration > 30) {
                    response.put("success", false);
                    response.put("message", "슬라이드 지속시간은 1~30초 사이로 입력하세요");
                    return response;
                }
                siteSettingService.saveSetting(
                        SiteSettingService.KEY_SLIDE_DURATION, slideDuration, "슬라이드 지속시간(초)");
            }

            if (popupDuration != null) {
                // 숫자 검증
                int duration = Integer.parseInt(popupDuration);
                if (duration < 1 || duration > 365) {
                    response.put("success", false);
                    response.put("message", "팝업 지속시간은 1~365일 사이로 입력하세요");
                    return response;
                }
                siteSettingService.saveSetting(
                        SiteSettingService.KEY_POPUP_DURATION, popupDuration, "팝업 지속시간(일)");
            }

            response.put("success", true);
            response.put("message", "설정이 저장되었습니다");
            return response;
        } catch (NumberFormatException e) {
            response.put("success", false);
            response.put("message", "올바른 숫자를 입력하세요");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "설정 저장 중 오류가 발생했습니다");
            return response;
        }
    }
}