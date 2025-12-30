package com.kh.shop.controller.admin;

import com.kh.shop.entity.Popup;
import com.kh.shop.service.PopupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/popup")
public class AdminPopupApiController {

    @Autowired
    private PopupService popupService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/create")
    public Map<String, Object> createPopup(
            @RequestParam String popupTitle,
            @RequestParam(required = false) String popupContent,
            @RequestParam(required = false) String popupLink,
            @RequestParam(required = false, defaultValue = "400") Integer popupWidth,
            @RequestParam(required = false, defaultValue = "500") Integer popupHeight,
            @RequestParam(required = false, defaultValue = "100") Integer popupTop,
            @RequestParam(required = false, defaultValue = "100") Integer popupLeft,
            @RequestParam(required = false, defaultValue = "0") Integer popupOrder,
            @RequestParam(required = false) MultipartFile popupImage,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (popupTitle == null || popupTitle.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "팝업 제목을 입력하세요");
            return response;
        }

        try {
            Popup popup = popupService.createPopup(popupTitle, popupContent, popupLink,
                    popupWidth, popupHeight, popupTop, popupLeft, popupOrder, popupImage);
            response.put("success", true);
            response.put("message", "팝업이 등록되었습니다");
            response.put("popupId", popup.getPopupId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "팝업 등록 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update/{popupId}")
    public Map<String, Object> updatePopup(
            @PathVariable Long popupId,
            @RequestParam String popupTitle,
            @RequestParam(required = false) String popupContent,
            @RequestParam(required = false) String popupLink,
            @RequestParam(required = false, defaultValue = "400") Integer popupWidth,
            @RequestParam(required = false, defaultValue = "500") Integer popupHeight,
            @RequestParam(required = false, defaultValue = "100") Integer popupTop,
            @RequestParam(required = false, defaultValue = "100") Integer popupLeft,
            @RequestParam(required = false, defaultValue = "0") Integer popupOrder,
            @RequestParam(required = false) MultipartFile popupImage,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (popupTitle == null || popupTitle.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "팝업 제목을 입력하세요");
            return response;
        }

        Optional<Popup> existingPopup = popupService.getPopupById(popupId);
        if (!existingPopup.isPresent()) {
            response.put("success", false);
            response.put("message", "존재하지 않는 팝업입니다");
            return response;
        }

        try {
            Popup popup = popupService.updatePopup(popupId, popupTitle, popupContent, popupLink,
                    popupWidth, popupHeight, popupTop, popupLeft, popupOrder, popupImage);
            response.put("success", true);
            response.put("message", "팝업이 수정되었습니다");
            response.put("popupId", popup.getPopupId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "팝업 수정 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/delete/{popupId}")
    public Map<String, Object> deletePopup(@PathVariable Long popupId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        try {
            popupService.deletePopup(popupId);
            response.put("success", true);
            response.put("message", "팝업이 삭제되었습니다");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "팝업 삭제 중 오류가 발생했습니다");
            return response;
        }
    }
}