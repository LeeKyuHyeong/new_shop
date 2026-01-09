package com.kh.shop.scheduler;

import com.kh.shop.repository.ProductViewStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductViewStatsBatchScheduler {

    private final ProductViewStatsRepository productViewStatsRepository;

    // 통계 데이터 보관 기간 (90일)
    private static final int RETENTION_DAYS = 90;

    /**
     * 매일 01:30에 일별 상품 조회수 통계 집계
     */
    @Scheduled(cron = "0 30 1 * * *")
    @Transactional(readOnly = true)
    public void aggregateProductViewStats() {
        log.info("========== [배치] 상품 조회수 집계 시작 ==========");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDate startDate = yesterday.minusDays(7);

            // 최근 7일간 인기 상품 조회
            List<Object[]> topProducts = productViewStatsRepository.findTopViewedProducts(startDate, yesterday);

            if (topProducts.isEmpty()) {
                log.info("[배치] 조회수 데이터가 없습니다.");
            } else {
                log.info("[배치] 최근 7일 인기 상품 TOP 10:");
                int rank = 1;
                for (Object[] row : topProducts) {
                    if (rank > 10) break;
                    Long productId = (Long) row[0];
                    Long viewCount = (Long) row[1];
                    log.info("  {}위: 상품ID {} ({}회 조회)", rank++, productId, viewCount);
                }

                log.info("[배치] 총 {}개 상품의 조회수 집계 완료", topProducts.size());
            }

        } catch (Exception e) {
            log.error("[배치] 상품 조회수 집계 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 상품 조회수 집계 종료 ==========");
    }

    /**
     * 매월 1일 03:00에 오래된 조회수 데이터 정리
     */
    @Scheduled(cron = "0 0 3 1 * *")
    @Transactional
    public void cleanupOldStats() {
        log.info("[배치] 오래된 조회수 데이터 정리 시작");

        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(RETENTION_DAYS);
            productViewStatsRepository.deleteByViewDateBefore(cutoffDate);
            log.info("[배치] {}일 이전 조회수 데이터 정리 완료", RETENTION_DAYS);
        } catch (Exception e) {
            log.error("[배치] 조회수 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String aggregateProductViewStatsManual() {
        log.info("[배치] 상품 조회수 집계 수동 실행");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate startDate = yesterday.minusDays(7);

        List<Object[]> topProducts = productViewStatsRepository.findTopViewedProducts(startDate, yesterday);

        StringBuilder result = new StringBuilder();
        result.append("인기 상품 TOP 5 (최근 7일): ");

        int rank = 1;
        for (Object[] row : topProducts) {
            if (rank > 5) break;
            Long productId = (Long) row[0];
            Long viewCount = (Long) row[1];
            result.append(rank++).append(".상품").append(productId).append("(").append(viewCount).append("회) ");
        }

        String resultStr = result.toString().trim();
        if (topProducts.isEmpty()) {
            resultStr = "조회수 데이터가 없습니다.";
        }

        log.info("[배치] {}", resultStr);
        return resultStr;
    }
}