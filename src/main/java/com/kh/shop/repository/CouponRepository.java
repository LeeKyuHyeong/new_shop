package com.kh.shop.repository;

import com.kh.shop.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    // 활성 쿠폰 조회
    List<Coupon> findByIsActiveTrueOrderByCreatedDateDesc();

    // 만료된 활성 쿠폰 조회 (비활성화 대상)
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.endDate < :now")
    List<Coupon> findExpiredActiveCoupons(@Param("now") LocalDateTime now);

    // 만료 예정 쿠폰 조회 (N일 이내)
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.endDate BETWEEN :now AND :futureDate")
    List<Coupon> findExpiringCoupons(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    // 만료된 쿠폰 일괄 비활성화
    @Modifying
    @Query("UPDATE Coupon c SET c.isActive = false, c.updatedDate = :now WHERE c.isActive = true AND c.endDate < :now")
    int deactivateExpiredCoupons(@Param("now") LocalDateTime now);
}