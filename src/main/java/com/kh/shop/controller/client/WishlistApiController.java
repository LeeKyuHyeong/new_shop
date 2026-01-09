package com.kh.shop.controller.client;

import com.kh.shop.entity.Wishlist;
import com.kh.shop.service.WishlistService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistApiController {

    private final WishlistService wishlistService;

    /**
     * 위시리스트 토글 (찜하기/취소)
     */
    @PostMapping("/toggle/{productId}")
    public ResponseEntity<Map<String, Object>> toggleWishlist(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("requireLogin", true);
            return ResponseEntity.ok(result);
        }

        boolean isAdded = wishlistService.toggleWishlist(userId, productId);
        long count = wishlistService.getWishlistCount(userId);

        result.put("success", true);
        result.put("isWished", isAdded);
        result.put("message", isAdded ? "위시리스트에 추가되었습니다." : "위시리스트에서 제거되었습니다.");
        result.put("wishlistCount", count);

        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트 추가
     */
    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToWishlist(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("requireLogin", true);
            return ResponseEntity.ok(result);
        }

        boolean success = wishlistService.addToWishlist(userId, productId);

        if (success) {
            result.put("success", true);
            result.put("message", "위시리스트에 추가되었습니다.");
        } else {
            result.put("success", false);
            result.put("message", "이미 위시리스트에 있는 상품입니다.");
        }

        result.put("wishlistCount", wishlistService.getWishlistCount(userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트 제거
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromWishlist(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        boolean success = wishlistService.removeFromWishlist(userId, productId);

        result.put("success", success);
        result.put("message", success ? "위시리스트에서 제거되었습니다." : "제거에 실패했습니다.");
        result.put("wishlistCount", wishlistService.getWishlistCount(userId));

        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트 ID로 제거
     */
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Map<String, Object>> removeById(
            @PathVariable Long wishlistId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(result);
        }

        boolean success = wishlistService.removeById(wishlistId, userId);

        result.put("success", success);
        result.put("message", success ? "위시리스트에서 제거되었습니다." : "제거에 실패했습니다.");
        result.put("wishlistCount", wishlistService.getWishlistCount(userId));

        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트 목록 조회
     */
    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getWishlist(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            result.put("items", List.of());
            return ResponseEntity.ok(result);
        }

        List<Wishlist> wishlist = wishlistService.getWishlistByUser(userId);

        List<Map<String, Object>> items = wishlist.stream().map(w -> {
            Map<String, Object> item = new HashMap<>();
            item.put("wishlistId", w.getWishlistId());
            item.put("productId", w.getProduct().getProductId());
            item.put("productName", w.getProduct().getProductName());
            item.put("thumbnailUrl", w.getProduct().getThumbnailUrl());
            item.put("productPrice", w.getProduct().getProductPrice());
            item.put("discountedPrice", w.getProduct().getDiscountedPrice());
            item.put("productDiscount", w.getProduct().getProductDiscount());
            item.put("productStock", w.getProduct().getProductStock());
            item.put("createdDate", w.getCreatedDate());
            if (w.getProduct().getCategory() != null) {
                item.put("categoryName", w.getProduct().getCategory().getCategoryName());
            }
            return item;
        }).collect(Collectors.toList());

        result.put("success", true);
        result.put("items", items);
        result.put("count", items.size());

        return ResponseEntity.ok(result);
    }

    /**
     * 위시리스트 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getWishlistCount(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("count", 0);
            return ResponseEntity.ok(result);
        }

        result.put("count", wishlistService.getWishlistCount(userId));
        return ResponseEntity.ok(result);
    }

    /**
     * 특정 상품 위시리스트 여부 확인
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkWishlist(
            @PathVariable Long productId,
            HttpSession session) {

        Map<String, Object> result = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            result.put("isWished", false);
            return ResponseEntity.ok(result);
        }

        boolean isWished = wishlistService.isInWishlist(userId, productId);
        result.put("isWished", isWished);

        return ResponseEntity.ok(result);
    }
}