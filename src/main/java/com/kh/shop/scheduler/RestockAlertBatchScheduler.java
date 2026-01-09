package com.kh.shop.scheduler;

import com.kh.shop.entity.Product;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.WishlistRepository;
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
public class RestockAlertBatchScheduler {

    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;

    /**
     * 매시 10분에 품절 상품 재입고 시 알림 발송
     */
    @Scheduled(cron = "0 10 * * * *")
    @Transactional(readOnly = true)
    public void sendRestockAlerts() {
        log.info("========== [배치] 재입고 알림 시작 ==========");

        try {
            // 최근 1시간 내 재입고된 상품 조회
            LocalDateTime since = LocalDateTime.now().minusHours(1);
            List<Product> restockedProducts = productRepository.findRecentlyRestockedProducts(since);

            if (restockedProducts.isEmpty()) {
                log.info("[배치] 최근 재입고된 상품이 없습니다.");
                return;
            }

            int alertCount = 0;

            for (Product product : restockedProducts) {
                // 해당 상품을 찜한 사용자 수 조회
                long wishCount = wishlistRepository.countByProduct(product);

                if (wishCount > 0) {
                    // 실제 환경에서는 이메일/푸시 알림 발송
                    log.info("[배치] 재입고 알림 대상 - 상품:{}, 재고:{}개, 찜한 사용자:{}명",
                            product.getProductName(),
                            product.getProductStock(),
                            wishCount);
                    alertCount++;
                }
            }

            log.info("[배치] 재입고 알림 완료: {}개 상품에 대한 알림 처리", alertCount);

        } catch (Exception e) {
            log.error("[배치] 재입고 알림 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 재입고 알림 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String sendRestockAlertsManual() {
        log.info("[배치] 재입고 알림 수동 실행");

        LocalDateTime since = LocalDateTime.now().minusHours(1);
        List<Product> restockedProducts = productRepository.findRecentlyRestockedProducts(since);

        int alertCount = 0;
        for (Product product : restockedProducts) {
            long wishCount = wishlistRepository.countByProduct(product);
            if (wishCount > 0) {
                alertCount++;
            }
        }

        String result = "재입고 상품: " + restockedProducts.size() + "개, 알림 대상: " + alertCount + "건";
        log.info("[배치] {}", result);
        return result;
    }
}