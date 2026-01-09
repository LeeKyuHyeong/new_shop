package com.kh.shop.repository;

import com.kh.shop.entity.Point;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    // 사용자의 포인트 내역 조회
    List<Point> findByUserOrderByCreatedDateDesc(User user);

    // 사용자의 사용 가능한 포인트 조회 (적립 + 미만료 + 잔여금액 있음)
    @Query("SELECT p FROM Point p WHERE p.user = :user AND p.pointType = 'EARN' AND p.isExpired = false AND p.remainingAmount > 0 AND (p.expireDate IS NULL OR p.expireDate > :now) ORDER BY p.expireDate ASC")
    List<Point> findUsablePointsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // 사용자의 총 사용 가능 포인트
    @Query("SELECT COALESCE(SUM(p.remainingAmount), 0) FROM Point p WHERE p.user = :user AND p.pointType = 'EARN' AND p.isExpired = false AND p.remainingAmount > 0 AND (p.expireDate IS NULL OR p.expireDate > :now)")
    Integer getTotalUsablePoints(@Param("user") User user, @Param("now") LocalDateTime now);

    // 만료 대상 포인트 조회 (만료일 지난 미만료 포인트)
    @Query("SELECT p FROM Point p WHERE p.pointType = 'EARN' AND p.isExpired = false AND p.remainingAmount > 0 AND p.expireDate IS NOT NULL AND p.expireDate < :now")
    List<Point> findExpiredPoints(@Param("now") LocalDateTime now);

    // 곧 만료될 포인트 조회 (N일 이내)
    @Query("SELECT p FROM Point p JOIN FETCH p.user WHERE p.pointType = 'EARN' AND p.isExpired = false AND p.remainingAmount > 0 AND p.expireDate BETWEEN :now AND :futureDate")
    List<Point> findExpiringPoints(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    // 만료 처리
    @Modifying
    @Query("UPDATE Point p SET p.isExpired = true WHERE p.pointType = 'EARN' AND p.isExpired = false AND p.remainingAmount > 0 AND p.expireDate IS NOT NULL AND p.expireDate < :now")
    int expirePoints(@Param("now") LocalDateTime now);
}