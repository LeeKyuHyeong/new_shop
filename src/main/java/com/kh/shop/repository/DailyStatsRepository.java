package com.kh.shop.repository;

import com.kh.shop.entity.DailyStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatsRepository extends JpaRepository<DailyStats, Long> {

    Optional<DailyStats> findByStatDate(LocalDate statDate);

    List<DailyStats> findByStatDateBetweenOrderByStatDateDesc(LocalDate startDate, LocalDate endDate);

    // 최근 N일 통계 조회
    @Query("SELECT d FROM DailyStats d WHERE d.statDate >= :startDate ORDER BY d.statDate DESC")
    List<DailyStats> findRecentStats(@Param("startDate") LocalDate startDate);

    // 월별 매출 합계
    @Query("SELECT SUM(d.orderAmount) FROM DailyStats d WHERE YEAR(d.statDate) = :year AND MONTH(d.statDate) = :month")
    Long getMonthlyOrderAmount(@Param("year") int year, @Param("month") int month);

    // 월별 주문 건수 합계
    @Query("SELECT SUM(d.orderCount) FROM DailyStats d WHERE YEAR(d.statDate) = :year AND MONTH(d.statDate) = :month")
    Long getMonthlyOrderCount(@Param("year") int year, @Param("month") int month);
}