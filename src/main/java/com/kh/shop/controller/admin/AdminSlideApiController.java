package com.kh.shop.controller.admin;

import com.kh.shop.entity.Slide;
import com.kh.shop.service.SlideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/slide")
public class AdminSlideApiController {

    @Autowired
    private SlideService slideService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/create")
    public Map<String, Object> createSlide(
            @RequestParam String slideTitle,
            @RequestParam(required = false) String slideDescription,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false, defaultValue = "5") Integer duration,
            @RequestParam(required = false, defaultValue = "0") Integer slideOrder,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam MultipartFile image,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (slideTitle == null || slideTitle.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "슬라이드 제목을 입력하세요");
            return response;
        }

        if (image == null || image.isEmpty()) {
            response.put("success", false);
            response.put("message", "이미지를 선택하세요");
            return response;
        }

        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);

            Slide slide = slideService.createSlide(
                    slideTitle, slideDescription, linkUrl, duration, slideOrder,
                    start, end, image);

            response.put("success", true);
            response.put("message", "슬라이드가 등록되었습니다");
            response.put("slideId", slide.getSlideId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "슬라이드 등록 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update/{slideId}")
    public Map<String, Object> updateSlide(
            @PathVariable Long slideId,
            @RequestParam String slideTitle,
            @RequestParam(required = false) String slideDescription,
            @RequestParam(required = false) String linkUrl,
            @RequestParam(required = false, defaultValue = "5") Integer duration,
            @RequestParam(required = false, defaultValue = "0") Integer slideOrder,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) MultipartFile image,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (slideTitle == null || slideTitle.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "슬라이드 제목을 입력하세요");
            return response;
        }

        Optional<Slide> existingSlide = slideService.getSlideById(slideId);
        if (!existingSlide.isPresent()) {
            response.put("success", false);
            response.put("message", "존재하지 않는 슬라이드입니다");
            return response;
        }

        try {
            LocalDateTime start = parseDateTime(startDate);
            LocalDateTime end = parseDateTime(endDate);

            Slide slide = slideService.updateSlide(
                    slideId, slideTitle, slideDescription, linkUrl, duration, slideOrder,
                    start, end, image);

            response.put("success", true);
            response.put("message", "슬라이드가 수정되었습니다");
            response.put("slideId", slide.getSlideId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "슬라이드 수정 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/delete/{slideId}")
    public Map<String, Object> deleteSlide(@PathVariable Long slideId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        try {
            slideService.deleteSlide(slideId);
            response.put("success", true);
            response.put("message", "슬라이드가 삭제되었습니다");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "슬라이드 삭제 중 오류가 발생했습니다");
            return response;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // datetime-local 형식: yyyy-MM-ddTHH:mm
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}