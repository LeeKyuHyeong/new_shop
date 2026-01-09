package com.kh.shop.scheduler;

import com.kh.shop.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredCouponBatchScheduler {

    private final CouponRepository couponRepository;

    /**
     * 매일 00:30에 만료된 쿠폰 비활성화 처리
     */
    @Scheduled(cron = "0 30 0 * * *")
    @Transactional
    public void deactivateExpiredCoupons() {
        log.info("========== [배치] 쿠폰 만료 처리 시작 ==========");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 만료된 쿠폰 비활성화
            int deactivatedCount = couponRepository.deactivateExpiredCoupons(now);

            if (deactivatedCount > 0) {
                log.info("[배치] {}개의 만료된 쿠폰이 비활성화되었습니다.", deactivatedCount);
            } else {
                log.info("[배치] 만료된 쿠폰이 없습니다.");
            }

        } catch (Exception e) {
            log.error("[배치] 쿠폰 만료 처리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 쿠폰 만료 처리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional
    public String deactivateExpiredCouponsManual() {
        log.info("[배치] 쿠폰 만료 처리 수동 실행");

        LocalDateTime now = LocalDateTime.now();
        int deactivatedCount = couponRepository.deactivateExpiredCoupons(now);

        String result = deactivatedCount + "개의 만료된 쿠폰이 비활성화되었습니다.";
        log.info("[배치] {}", result);
        return result;
    }
}