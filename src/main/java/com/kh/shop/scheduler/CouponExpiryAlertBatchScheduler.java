package com.kh.shop.scheduler;

import com.kh.shop.entity.UserCoupon;
import com.kh.shop.repository.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponExpiryAlertBatchScheduler {

    private final UserCouponRepository userCouponRepository;

    // 만료 예정 기준 일수 (3일)
    private static final int EXPIRY_ALERT_DAYS = 3;

    /**
     * 매일 09:00에 3일 내 만료 예정 쿠폰 알림
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void sendCouponExpiryAlerts() {
        log.info("========== [배치] 쿠폰 만료 예정 알림 시작 ==========");

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime futureDate = now.plusDays(EXPIRY_ALERT_DAYS);

            // 만료 예정 쿠폰 조회
            List<UserCoupon> expiringCoupons = userCouponRepository.findExpiringUserCoupons(now, futureDate);

            if (expiringCoupons.isEmpty()) {
                log.info("[배치] 만료 예정 쿠폰이 없습니다.");
            } else {
                int alertCount = 0;

                for (UserCoupon userCoupon : expiringCoupons) {
                    // 실제 환경에서는 이메일/푸시 알림 발송
                    log.info("[배치] 쿠폰 만료 예정 알림 - 사용자:{}, 쿠폰:{}, 만료일:{}",
                            userCoupon.getUser().getUserId(),
                            userCoupon.getCoupon().getCouponName(),
                            userCoupon.getExpireDate());
                    alertCount++;
                }

                log.info("[배치] {}건의 쿠폰 만료 예정 알림이 발송되었습니다. (기준: {}일 이내)",
                        alertCount, EXPIRY_ALERT_DAYS);
            }

        } catch (Exception e) {
            log.error("[배치] 쿠폰 만료 예정 알림 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 쿠폰 만료 예정 알림 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String sendCouponExpiryAlertsManual() {
        log.info("[배치] 쿠폰 만료 예정 알림 수동 실행");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(EXPIRY_ALERT_DAYS);

        List<UserCoupon> expiringCoupons = userCouponRepository.findExpiringUserCoupons(now, futureDate);

        String result = expiringCoupons.size() + "건의 쿠폰 만료 예정 알림 발송 대상";
        log.info("[배치] {}", result);
        return result;
    }
}