package com.kh.shop.service;

import com.kh.shop.entity.*;
import com.kh.shop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewImageRepository reviewImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // 리뷰 작성
    public Review createReview(Long productId, String userId, Integer rating, String content,
                               Long orderId, List<MultipartFile> images) throws IOException {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 이미 리뷰 작성했는지 확인
        if (reviewRepository.existsByProductAndUserAndIsDeletedFalse(product, user)) {
            throw new RuntimeException("이미 이 상품에 리뷰를 작성하셨습니다.");
        }

        Review review = new Review();
        review.setProduct(product);
        review.setUser(user);
        review.setRating(rating);
        review.setContent(content);

        // 주문 연결 (있는 경우)
        if (orderId != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                review.setOrder(order);
            }
        }

        review = reviewRepository.save(review);

        // 이미지 저장
        if (images != null && !images.isEmpty()) {
            saveReviewImages(review, images);
        }

        return review;
    }

    // 리뷰 이미지 저장
    private void saveReviewImages(Review review, List<MultipartFile> images) throws IOException {
        String reviewUploadDir = uploadDir + "/reviews/" + review.getReviewId();
        Path uploadPath = Paths.get(reviewUploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        int sortOrder = 0;
        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = UUID.randomUUID().toString() + extension;

            Path filePath = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), filePath);

            ReviewImage reviewImage = new ReviewImage();
            reviewImage.setReview(review);
            reviewImage.setImageUrl("/uploads/reviews/" + review.getReviewId() + "/" + savedName);
            reviewImage.setOriginalName(originalName);
            reviewImage.setSortOrder(sortOrder++);

            reviewImageRepository.save(reviewImage);
            review.addImage(reviewImage);
        }
    }

    // 리뷰 수정
    public Review updateReview(Long reviewId, String userId, Integer rating, String content,
                               List<MultipartFile> newImages, List<Long> deleteImageIds) throws IOException {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 작성자 확인
        if (!review.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.setRating(rating);
        review.setContent(content);

        // 삭제할 이미지 처리
        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                ReviewImage image = reviewImageRepository.findById(imageId).orElse(null);
                if (image != null && image.getReview().getReviewId().equals(reviewId)) {
                    // 파일 삭제
                    deleteImageFile(image.getImageUrl());
                    review.removeImage(image);
                    reviewImageRepository.delete(image);
                }
            }
        }

        // 새 이미지 저장
        if (newImages != null && !newImages.isEmpty()) {
            saveReviewImages(review, newImages);
        }

        return reviewRepository.save(review);
    }

    // 리뷰 삭제 (소프트 삭제)
    public void deleteReview(Long reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        // 작성자 확인
        if (!review.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        review.setIsDeleted(true);
        reviewRepository.save(review);
    }

    // 관리자용 리뷰 삭제
    public void adminDeleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
        review.setIsDeleted(true);
        reviewRepository.save(review);
    }

    // 관리자 답변 작성
    public Review addAdminReply(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

        review.setAdminReply(reply);
        review.setAdminReplyDate(LocalDateTime.now());

        return reviewRepository.save(review);
    }

    // 상품별 리뷰 조회
    @Transactional(readOnly = true)
    public List<Review> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        return reviewRepository.findByProductAndIsDeletedFalseOrderByCreatedDateDesc(product);
    }

    // 상품별 리뷰 페이징 조회
    @Transactional(readOnly = true)
    public Page<Review> getProductReviews(Long productId, int page, int size) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByProductAndIsDeletedFalseOrderByCreatedDateDesc(product, pageable);
    }

    // 상품 리뷰 통계
    @Transactional(readOnly = true)
    public Map<String, Object> getProductReviewStats(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        Map<String, Object> stats = new HashMap<>();

        long totalCount = reviewRepository.countByProductAndIsDeletedFalse(product);
        Double avgRating = reviewRepository.getAverageRatingByProduct(product);
        List<Object[]> distribution = reviewRepository.getRatingDistributionByProduct(product);

        stats.put("totalCount", totalCount);
        stats.put("averageRating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        // 별점 분포 맵 생성
        Map<Integer, Long> ratingDist = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            ratingDist.put(i, 0L);
        }
        for (Object[] row : distribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            ratingDist.put(rating, count);
        }
        stats.put("ratingDistribution", ratingDist);

        return stats;
    }

    // 사용자 리뷰 조회
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return reviewRepository.findByUserAndIsDeletedFalseOrderByCreatedDateDesc(user);
    }

    // 리뷰 상세 조회
    @Transactional(readOnly = true)
    public Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));
    }

    // 리뷰 작성 가능 여부 확인
    @Transactional(readOnly = true)
    public boolean canWriteReview(Long productId, String userId) {
        Product product = productRepository.findById(productId).orElse(null);
        User user = userRepository.findByUserId(userId).orElse(null);

        if (product == null || user == null) {
            return false;
        }

        // 이미 리뷰 작성했는지 확인
        return !reviewRepository.existsByProductAndUserAndIsDeletedFalse(product, user);
    }

    // 이미지 파일 삭제
    private void deleteImageFile(String imageUrl) {
        try {
            String filePath = uploadDir + imageUrl.replace("/uploads", "");
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            // 파일 삭제 실패는 무시
        }
    }

    // 관리자용 - 모든 리뷰 조회
    @Transactional(readOnly = true)
    public Page<Review> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findAllByOrderByCreatedDateDesc(pageable);
    }

    // 관리자용 - 답변 안 된 리뷰 조회
    @Transactional(readOnly = true)
    public Page<Review> getUnansweredReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reviewRepository.findByAdminReplyIsNullAndIsDeletedFalseOrderByCreatedDateDesc(pageable);
    }
}