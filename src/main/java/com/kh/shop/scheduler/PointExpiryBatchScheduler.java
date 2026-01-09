package com.kh.shop.scheduler;

import com.kh.shop.entity.Point;
import com.kh.shop.repository.PointRepository;
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
public class PointExpiryBatchScheduler {

    private final PointRepository pointRepository;

    /**
     * 매일 00:00에 유효기간 지난 적립금 소멸
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expirePoints() {
        log.info("========== [배치] 포인트 만료 처리 시작 ==========");

        try {
            LocalDateTime now = LocalDateTime.now();

            // 만료 대상 포인트 조회
            List<Point> expiredPoints = pointRepository.findExpiredPoints(now);

            if (expiredPoints.isEmpty()) {
                log.info("[배치] 만료 대상 포인트가 없습니다.");
            } else {
                int totalExpiredAmount = 0;

                for (Point point : expiredPoints) {
                    // 만료 처리
                    point.setIsExpired(true);
                    totalExpiredAmount += point.getRemainingAmount();

                    // 만료 내역 로그
                    log.info("[배치] 포인트 만료 - 사용자:{}, 금액:{}원, 만료일:{}",
                            point.getUser().getUserId(),
                            point.getRemainingAmount(),
                            point.getExpireDate());

                    // 잔여 금액 0으로 설정
                    point.setRemainingAmount(0);
                }

                pointRepository.saveAll(expiredPoints);

                log.info("[배치] 총 {}건의 포인트가 만료 처리되었습니다. (총 {}원)",
                        expiredPoints.size(), totalExpiredAmount);
            }

        } catch (Exception e) {
            log.error("[배치] 포인트 만료 처리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 포인트 만료 처리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional
    public String expirePointsManual() {
        log.info("[배치] 포인트 만료 처리 수동 실행");

        LocalDateTime now = LocalDateTime.now();
        List<Point> expiredPoints = pointRepository.findExpiredPoints(now);

        int totalExpiredAmount = 0;
        for (Point point : expiredPoints) {
            point.setIsExpired(true);
            totalExpiredAmount += point.getRemainingAmount();
            point.setRemainingAmount(0);
        }

        if (!expiredPoints.isEmpty()) {
            pointRepository.saveAll(expiredPoints);
        }

        String result = expiredPoints.size() + "건의 포인트가 만료 처리되었습니다. (총 " + totalExpiredAmount + "원)";
        log.info("[배치] {}", result);
        return result;
    }
}