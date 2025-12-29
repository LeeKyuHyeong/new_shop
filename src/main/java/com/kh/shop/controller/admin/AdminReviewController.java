package com.kh.shop.controller.admin;

import com.kh.shop.entity.Review;
import com.kh.shop.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/review")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 목록
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "all") String filter,
            Model model,
            HttpSession session) {

        if (session.getAttribute("loggedInUser") == null ||
                !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/login";
        }

        Page<Review> reviews;
        if ("unanswered".equals(filter)) {
            reviews = reviewService.getUnansweredReviews(page, size);
        } else {
            reviews = reviewService.getAllReviews(page, size);
        }

        model.addAttribute("reviews", reviews.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviews.getTotalPages());
        model.addAttribute("totalElements", reviews.getTotalElements());
        model.addAttribute("filter", filter);

        return "admin/review/list";
    }

    // 리뷰 상세
    @GetMapping("/{reviewId}")
    public String detail(@PathVariable Long reviewId, Model model, HttpSession session) {

        if (session.getAttribute("loggedInUser") == null ||
                !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/login";
        }

        Review review = reviewService.getReview(reviewId);
        model.addAttribute("review", review);

        return "admin/review/detail";
    }

    // 관리자 답변 등록
    @PostMapping("/reply/{reviewId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addReply(
            @PathVariable Long reviewId,
            @RequestParam String reply,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("loggedInUser") == null ||
                !"ADMIN".equals(session.getAttribute("userRole"))) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.ok(response);
        }

        try {
            reviewService.addAdminReply(reviewId, reply);
            response.put("success", true);
            response.put("message", "답변이 등록되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @PostMapping("/delete/{reviewId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long reviewId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (session.getAttribute("loggedInUser") == null ||
                !"ADMIN".equals(session.getAttribute("userRole"))) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.ok(response);
        }

        try {
            reviewService.adminDeleteReview(reviewId);
            response.put("success", true);
            response.put("message", "리뷰가 삭제되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}