package com.kh.shop.repository;

import com.kh.shop.entity.User;
import com.kh.shop.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {

    // 사용자의 사용 가능한 쿠폰 조회
    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.coupon c WHERE uc.user = :user AND uc.isUsed = false AND (uc.expireDate IS NULL OR uc.expireDate > :now) AND c.isActive = true")
    List<UserCoupon> findUsableCouponsByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    // 사용자의 모든 쿠폰 조회
    List<UserCoupon> findByUserOrderByCreatedDateDesc(User user);

    // 만료 예정 쿠폰 조회 (N일 이내, 미사용)
    @Query("SELECT uc FROM UserCoupon uc JOIN FETCH uc.user JOIN FETCH uc.coupon WHERE uc.isUsed = false AND uc.expireDate BETWEEN :now AND :futureDate")
    List<UserCoupon> findExpiringUserCoupons(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    // 사용자별 특정 쿠폰 발급 여부 확인
    boolean existsByUserAndCouponCouponId(User user, Long couponId);
}