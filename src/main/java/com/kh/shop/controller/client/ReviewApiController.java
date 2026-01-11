package com.kh.shop.controller.client;

import com.kh.shop.entity.Review;
import com.kh.shop.entity.ReviewImage;
import com.kh.shop.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/review")
public class ReviewApiController {

    @Autowired
    private ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createReview(
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) List<MultipartFile> images,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            response.put("needLogin", true);
            return ResponseEntity.ok(response);
        }

        try {
            Review review = reviewService.createReview(productId, userId, rating, content, orderId, images);
            response.put("success", true);
            response.put("message", "리뷰가 등록되었습니다.");
            response.put("reviewId", review.getReviewId());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 수정
    @PostMapping("/update/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable Long reviewId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) List<MultipartFile> newImages,
            @RequestParam(required = false) List<Long> deleteImageIds,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        try {
            reviewService.updateReview(reviewId, userId, rating, content, newImages, deleteImageIds);
            response.put("success", true);
            response.put("message", "리뷰가 수정되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 삭제
    @PostMapping("/delete/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable Long reviewId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        try {
            reviewService.deleteReview(reviewId, userId);
            response.put("success", true);
            response.put("message", "리뷰가 삭제되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 상품별 리뷰 목록 조회
    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String currentUserId = (String) session.getAttribute("loggedInUser");

        try {
            Page<Review> reviewPage = reviewService.getProductReviews(productId, page, size);
            Map<String, Object> stats = reviewService.getProductReviewStats(productId);

            // 리뷰 데이터 변환
            List<Map<String, Object>> reviews = reviewPage.getContent().stream()
                    .map(review -> convertReviewToMap(review, currentUserId))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("reviews", reviews);
            response.put("stats", stats);
            response.put("totalPages", reviewPage.getTotalPages());
            response.put("totalElements", reviewPage.getTotalElements());
            response.put("currentPage", page);
            response.put("hasNext", reviewPage.hasNext());
            response.put("hasPrevious", reviewPage.hasPrevious());

            // 리뷰 작성 가능 여부
            if (currentUserId != null) {
                boolean hasPurchased = reviewService.hasPurchased(productId, currentUserId);
                boolean canWrite = reviewService.canWriteReview(productId, currentUserId);
                response.put("canWrite", canWrite);
                response.put("hasPurchased", hasPurchased);
            } else {
                response.put("canWrite", false);
                response.put("hasPurchased", false);
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> getReview(
            @PathVariable Long reviewId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String currentUserId = (String) session.getAttribute("loggedInUser");

        try {
            Review review = reviewService.getReview(reviewId);
            response.put("success", true);
            response.put("review", convertReviewToMap(review, currentUserId));
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 작성 가능 여부 확인
    @GetMapping("/can-write/{productId}")
    public ResponseEntity<Map<String, Object>> canWriteReview(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            response.put("canWrite", false);
            response.put("needLogin", true);
            return ResponseEntity.ok(response);
        }

        boolean canWrite = reviewService.canWriteReview(productId, userId);
        response.put("canWrite", canWrite);
        response.put("needLogin", false);

        return ResponseEntity.ok(response);
    }

    // Review 엔티티를 Map으로 변환
    private Map<String, Object> convertReviewToMap(Review review, String currentUserId) {
        Map<String, Object> map = new HashMap<>();

        map.put("reviewId", review.getReviewId());
        map.put("rating", review.getRating());
        map.put("ratingStars", review.getRatingStars());
        map.put("content", review.getContent());
        map.put("maskedUserName", review.getMaskedUserName());
        map.put("createdDate", review.getCreatedDate().toString());
        map.put("adminReply", review.getAdminReply());

        if (review.getAdminReplyDate() != null) {
            map.put("adminReplyDate", review.getAdminReplyDate().toString());
        }

        // 이미지 목록
        List<Map<String, Object>> images = review.getImages().stream()
                .map(img -> {
                    Map<String, Object> imgMap = new HashMap<>();
                    imgMap.put("imageId", img.getImageId());
                    imgMap.put("imageUrl", img.getImageUrl());
                    return imgMap;
                })
                .collect(Collectors.toList());
        map.put("images", images);

        // 본인 리뷰인지 확인
        boolean isOwner = currentUserId != null && review.getUser().getUserId().equals(currentUserId);
        map.put("isOwner", isOwner);

        return map;
    }
}