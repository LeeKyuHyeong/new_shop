package com.kh.shop.scheduler;

import com.kh.shop.entity.DailyStats;
import com.kh.shop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatsAggregateBatchScheduler {

    private final DailyStatsRepository dailyStatsRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;

    /**
     * 매일 01:00에 전일 통계 데이터 집계
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void aggregateDailyStats() {
        log.info("========== [배치] 통계 데이터 집계 시작 ==========");

        try {
            // 전일 날짜
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startOfDay = yesterday.atStartOfDay();
            LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

            // 이미 집계된 데이터가 있는지 확인
            Optional<DailyStats> existingStats = dailyStatsRepository.findByStatDate(yesterday);
            DailyStats stats = existingStats.orElse(DailyStats.builder().statDate(yesterday).build());

            // 주문 통계 집계
            long orderCount = orderRepository.findAll().stream()
                    .filter(o -> o.getCreatedDate().isAfter(startOfDay) && o.getCreatedDate().isBefore(endOfDay))
                    .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                    .count();

            long orderAmount = orderRepository.findAll().stream()
                    .filter(o -> o.getCreatedDate().isAfter(startOfDay) && o.getCreatedDate().isBefore(endOfDay))
                    .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                    .mapToLong(o -> o.getFinalPrice() != null ? o.getFinalPrice() : 0)
                    .sum();

            long cancelledCount = orderRepository.findAll().stream()
                    .filter(o -> o.getCancelledAt() != null && o.getCancelledAt().isAfter(startOfDay) && o.getCancelledAt().isBefore(endOfDay))
                    .count();

            stats.setOrderCount((int) orderCount);
            stats.setOrderAmount(orderAmount);
            stats.setCancelledCount((int) cancelledCount);

            // 회원 통계 집계
            long newUserCount = userRepository.findAll().stream()
                    .filter(u -> u.getCreatedDate().isAfter(startOfDay) && u.getCreatedDate().isBefore(endOfDay))
                    .count();

            long totalUserCount = userRepository.count();

            stats.setNewUserCount((int) newUserCount);
            stats.setTotalUserCount((int) totalUserCount);

            // 상품 통계 집계
            long newProductCount = productRepository.findAll().stream()
                    .filter(p -> p.getCreatedDate().isAfter(startOfDay) && p.getCreatedDate().isBefore(endOfDay))
                    .count();

            long totalProductCount = productRepository.findByUseYnOrderByProductOrderAsc("Y").size();

            stats.setNewProductCount((int) newProductCount);
            stats.setTotalProductCount((int) totalProductCount);

            // 리뷰 통계 집계
            long newReviewCount = reviewRepository.findAll().stream()
                    .filter(r -> r.getCreatedDate().isAfter(startOfDay) && r.getCreatedDate().isBefore(endOfDay))
                    .count();

            long totalReviewCount = reviewRepository.count();

            stats.setNewReviewCount((int) newReviewCount);
            stats.setTotalReviewCount((int) totalReviewCount);

            dailyStatsRepository.save(stats);

            log.info("[배치] {} 통계 집계 완료 - 주문:{}건/{}원, 신규회원:{}명, 신규상품:{}개, 신규리뷰:{}개",
                    yesterday, orderCount, orderAmount, newUserCount, newProductCount, newReviewCount);

        } catch (Exception e) {
            log.error("[배치] 통계 데이터 집계 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 통계 데이터 집계 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional
    public String aggregateDailyStatsManual() {
        log.info("[배치] 통계 데이터 집계 수동 실행");

        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.plusDays(1).atStartOfDay();

        Optional<DailyStats> existingStats = dailyStatsRepository.findByStatDate(yesterday);
        DailyStats stats = existingStats.orElse(DailyStats.builder().statDate(yesterday).build());

        long orderCount = orderRepository.findAll().stream()
                .filter(o -> o.getCreatedDate().isAfter(startOfDay) && o.getCreatedDate().isBefore(endOfDay))
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                .count();

        long newUserCount = userRepository.findAll().stream()
                .filter(u -> u.getCreatedDate().isAfter(startOfDay) && u.getCreatedDate().isBefore(endOfDay))
                .count();

        stats.setOrderCount((int) orderCount);
        stats.setNewUserCount((int) newUserCount);
        stats.setTotalUserCount((int) userRepository.count());
        stats.setTotalProductCount(productRepository.findByUseYnOrderByProductOrderAsc("Y").size());

        dailyStatsRepository.save(stats);

        String result = yesterday + " 통계 집계 완료 - 주문:" + orderCount + "건, 신규회원:" + newUserCount + "명";
        log.info("[배치] {}", result);
        return result;
    }
}