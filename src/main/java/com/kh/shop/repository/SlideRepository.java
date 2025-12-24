package com.kh.shop.repository;

import com.kh.shop.entity.Slide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlideRepository extends JpaRepository<Slide, Long> {

    // 사용중인 슬라이드 전체 조회
    List<Slide> findByUseYnOrderBySlideOrderAsc(String useYn);

    // 현재 활성화된 슬라이드 조회 (기간 체크)
    @Query("SELECT s FROM Slide s WHERE s.useYn = 'Y' " +
            "AND (s.startDate IS NULL OR s.startDate <= :now) " +
            "AND (s.endDate IS NULL OR s.endDate >= :now) " +
            "ORDER BY s.slideOrder ASC")
    List<Slide> findActiveSlides(@Param("now") LocalDateTime now);
}