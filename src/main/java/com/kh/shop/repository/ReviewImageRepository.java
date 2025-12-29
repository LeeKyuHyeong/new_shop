package com.kh.shop.repository;

import com.kh.shop.entity.ReviewImage;
import com.kh.shop.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {

    List<ReviewImage> findByReviewOrderBySortOrderAsc(Review review);

    void deleteByReview(Review review);
}