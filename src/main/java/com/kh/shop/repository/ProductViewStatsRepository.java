package com.kh.shop.repository;

import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductViewStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductViewStatsRepository extends JpaRepository<ProductViewStats, Long> {

    // 특정 상품의 특정 날짜 조회수
    Optional<ProductViewStats> findByProductAndViewDate(Product product, LocalDate viewDate);

    // 특정 상품의 조회수 통계 (기간별)
    List<ProductViewStats> findByProductAndViewDateBetweenOrderByViewDateDesc(Product product, LocalDate startDate, LocalDate endDate);

    // 특정 날짜의 인기 상품 (조회수 기준)
    List<ProductViewStats> findByViewDateOrderByViewCountDesc(LocalDate viewDate);

    // 기간별 상품 조회수 합계
    @Query("SELECT pvs.product.productId, SUM(pvs.viewCount) as totalViews FROM ProductViewStats pvs WHERE pvs.viewDate BETWEEN :startDate AND :endDate GROUP BY pvs.product.productId ORDER BY totalViews DESC")
    List<Object[]> findTopViewedProducts(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 오래된 통계 삭제
    void deleteByViewDateBefore(LocalDate date);
}