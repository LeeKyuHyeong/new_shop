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
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderStatusBatchScheduler {

    private final OrderRepository orderRepository;
    private final Random random = new Random();

    /**
     * 매시 20분에 주문 상태 자동 변경
     * 상태 흐름: PAID → PREPARING → SHIPPING → DELIVERED
     */
    @Scheduled(cron = "0 20 * * * *")
    @Transactional
    public void updateOrderStatus() {
        log.info("========== [배치] 주문 상태 변경 시작 ==========");

        try {
            int updatedCount = 0;

            // 1. PAID → PREPARING (결제완료 → 상품준비중)
            updatedCount += updateStatus("PAID", "PREPARING", null);

            // 2. PREPARING → SHIPPING (상품준비중 → 배송중)
            updatedCount += updateStatus("PREPARING", "SHIPPING", "shippedAt");

            // 3. SHIPPING → DELIVERED (배송중 → 배송완료)
            updatedCount += updateStatus("SHIPPING", "DELIVERED", "deliveredAt");

            log.info("[배치] 총 {} 건의 주문 상태 변경 완료", updatedCount);

        } catch (Exception e) {
            log.error("[배치] 주문 상태 변경 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 주문 상태 변경 종료 ==========");
    }

    /**
     * 특정 상태의 주문을 다음 상태로 변경
     * 50% 확률로 변경 (자연스러운 흐름을 위해)
     */
    private int updateStatus(String fromStatus, String toStatus, String timestampField) {
        List<Order> orders = orderRepository.findByOrderStatusAndUseYnOrderByCreatedDateDesc(fromStatus, "Y");

        int count = 0;
        for (Order order : orders) {
            // 50% 확률로 상태 변경 (랜덤하게 진행)
            if (random.nextBoolean()) {
                order.setOrderStatus(toStatus);

                // 타임스탬프 설정
                LocalDateTime now = LocalDateTime.now();
                switch (toStatus) {
                    case "SHIPPING":
                        order.setShippedAt(now);
                        break;
                    case "DELIVERED":
                        order.setDeliveredAt(now);
                        break;
                }

                orderRepository.save(order);
                count++;

                log.info("[배치] 주문 #{} 상태 변경: {} → {}",
                        order.getOrderNumber(), fromStatus, toStatus);
            }
        }

        return count;
    }
}