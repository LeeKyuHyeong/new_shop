package com.kh.shop.scheduler;

import com.kh.shop.entity.Order;
import com.kh.shop.repository.OrderRepository;
import com.kh.shop.repository.ReviewRepository;
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
public class ReviewRequestBatchScheduler {

    private final OrderRepository orderRepository;
    private final ReviewRepository reviewRepository;

    // 리뷰 요청 기준 일수 (배송완료 후 7일)
    private static final int REVIEW_REQUEST_DAYS = 7;

    /**
     * 매일 10:00에 배송완료 7일 후 리뷰 작성 요청
     */
    @Scheduled(cron = "0 0 10 * * *")
    @Transactional(readOnly = true)
    public void sendReviewRequests() {
        log.info("========== [배치] 리뷰 작성 요청 시작 ==========");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(REVIEW_REQUEST_DAYS);

            // 배송완료 후 7일 경과한 주문 조회
            List<Order> targetOrders = orderRepository.findDeliveredOrdersForReview(cutoffDate);

            if (targetOrders.isEmpty()) {
                log.info("[배치] 리뷰 요청 대상 주문이 없습니다.");
            } else {
                int requestCount = 0;

                for (Order order : targetOrders) {
                    // 이미 리뷰를 작성했는지 확인
                    boolean hasReview = reviewRepository.existsByOrderAndIsDeletedFalse(order);

                    if (!hasReview) {
                        // 실제 환경에서는 이메일/푸시 알림 발송
                        log.info("[배치] 리뷰 요청 대상 - 주문번호:{}, 사용자:{}, 배송완료일:{}",
                                order.getOrderNumber(),
                                order.getUser().getUserId(),
                                order.getDeliveredAt());
                        requestCount++;
                    }
                }

                log.info("[배치] 리뷰 요청 발송 완료: {}건 (기준: 배송완료 {}일 경과)", requestCount, REVIEW_REQUEST_DAYS);
            }

        } catch (Exception e) {
            log.error("[배치] 리뷰 작성 요청 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 리뷰 작성 요청 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String sendReviewRequestsManual() {
        log.info("[배치] 리뷰 작성 요청 수동 실행");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(REVIEW_REQUEST_DAYS);
        List<Order> targetOrders = orderRepository.findDeliveredOrdersForReview(cutoffDate);

        int requestCount = 0;
        for (Order order : targetOrders) {
            boolean hasReview = reviewRepository.existsByOrderAndIsDeletedFalse(order);
            if (!hasReview) {
                requestCount++;
            }
        }

        String result = "리뷰 요청 대상: " + requestCount + "건";
        log.info("[배치] {}", result);
        return result;
    }
}