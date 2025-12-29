package com.kh.shop.repository;

import com.kh.shop.entity.Review;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import com.kh.shop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 상품별 리뷰 목록 (삭제되지 않은 것만)
    List<Review> findByProductAndIsDeletedFalseOrderByCreatedDateDesc(Product product);

    // 상품별 리뷰 페이징
    Page<Review> findByProductAndIsDeletedFalseOrderByCreatedDateDesc(Product product, Pageable pageable);

    // 사용자별 리뷰 목록
    List<Review> findByUserAndIsDeletedFalseOrderByCreatedDateDesc(User user);

    // 상품별 리뷰 개수
    long countByProductAndIsDeletedFalse(Product product);

    // 상품별 평균 별점
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product = :product AND r.isDeleted = false")
    Double getAverageRatingByProduct(@Param("product") Product product);

    // 상품별 별점 분포
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product = :product AND r.isDeleted = false GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProduct(@Param("product") Product product);

    // 특정 주문에 대한 리뷰 존재 여부
    boolean existsByOrderAndIsDeletedFalse(Order order);

    // 특정 사용자가 특정 상품에 리뷰를 작성했는지
    boolean existsByProductAndUserAndIsDeletedFalse(Product product, User user);

    // 특정 주문 아이템에 대한 리뷰 조회
    Optional<Review> findByOrderAndProductAndIsDeletedFalse(Order order, Product product);

    // 관리자용 - 모든 리뷰 (삭제된 것 포함)
    Page<Review> findAllByOrderByCreatedDateDesc(Pageable pageable);

    // 관리자용 - 답변 안 된 리뷰
    Page<Review> findByAdminReplyIsNullAndIsDeletedFalseOrderByCreatedDateDesc(Pageable pageable);

    // 이미지가 있는 리뷰만
    @Query("SELECT r FROM Review r WHERE r.product = :product AND r.isDeleted = false AND SIZE(r.images) > 0 ORDER BY r.createdDate DESC")
    List<Review> findByProductWithImagesOrderByCreatedDateDesc(@Param("product") Product product);
}