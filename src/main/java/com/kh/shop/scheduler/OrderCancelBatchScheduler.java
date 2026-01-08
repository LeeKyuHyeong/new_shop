package com.kh.shop.scheduler;

import com.kh.shop.entity.Order;
import com.kh.shop.repository.OrderRepository;
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
public class OrderCancelBatchScheduler {

    private final OrderRepository orderRepository;

    // 미결제 기준 시간 (24시간)
    private static final int UNPAID_HOURS = 24;

    /**
     * 매시 30분에 24시간 이상 미결제 주문 자동 취소
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void cancelUnpaidOrders() {
        log.info("========== [배치] 미결제 주문 취소 시작 ==========");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusHours(UNPAID_HOURS);
            List<Order> unpaidOrders = orderRepository.findUnpaidOrders(cutoffDate);

            if (unpaidOrders.isEmpty()) {
                log.info("[배치] 취소할 미결제 주문이 없습니다.");
            } else {
                int count = 0;
                for (Order order : unpaidOrders) {
                    order.setOrderStatus("CANCELLED");
                    order.setPaymentStatus("CANCELLED");
                    order.setCancelledAt(LocalDateTime.now());
                    order.setCancelReason("24시간 이상 미결제로 자동 취소");
                    orderRepository.save(order);
                    count++;
                }

                log.info("[배치] {}개의 미결제 주문이 자동 취소되었습니다. (기준: {}시간 이상)", count, UNPAID_HOURS);
            }

        } catch (Exception e) {
            log.error("[배치] 미결제 주문 취소 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 미결제 주문 취소 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     * @return 취소된 주문 개수
     */
    @Transactional
    public int cancelUnpaidOrdersManual() {
        log.info("[배치] 미결제 주문 취소 수동 실행");

        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(UNPAID_HOURS);
        List<Order> unpaidOrders = orderRepository.findUnpaidOrders(cutoffDate);

        if (unpaidOrders.isEmpty()) {
            log.info("[배치] 취소할 미결제 주문이 없습니다.");
            return 0;
        }

        int count = 0;
        for (Order order : unpaidOrders) {
            order.setOrderStatus("CANCELLED");
            order.setPaymentStatus("CANCELLED");
            order.setCancelledAt(LocalDateTime.now());
            order.setCancelReason("24시간 이상 미결제로 자동 취소");
            orderRepository.save(order);
            count++;
        }

        log.info("[배치] {}개의 미결제 주문이 자동 취소되었습니다.", count);
        return count;
    }

    /**
     * 미결제 주문 개수 조회 (미리보기용)
     */
    @Transactional(readOnly = true)
    public int countUnpaidOrders() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusHours(UNPAID_HOURS);
        return orderRepository.findUnpaidOrders(cutoffDate).size();
    }
}