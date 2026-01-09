package com.kh.shop.scheduler;

import com.kh.shop.entity.Product;
import com.kh.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LowStockAlertBatchScheduler {

    private final ProductRepository productRepository;

    // 재고 부족 기준 (10개 이하)
    private static final int LOW_STOCK_THRESHOLD = 10;

    /**
     * 매일 09:00에 재고 부족 상품 알림
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional(readOnly = true)
    public void checkLowStockProducts() {
        log.info("========== [배치] 재고 부족 알림 시작 ==========");

        try {
            // 재고 부족 상품 조회
            List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);

            // 품절 상품 조회
            List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();

            if (lowStockProducts.isEmpty() && outOfStockProducts.isEmpty()) {
                log.info("[배치] 재고 부족/품절 상품이 없습니다.");
            } else {
                // 로그 기록 (실제 환경에서는 이메일/슬랙 알림 등으로 대체)
                log.warn("[배치] 품절 상품: {}개", outOfStockProducts.size());
                for (Product p : outOfStockProducts) {
                    log.warn("  - [품절] ID:{}, 상품명:{}", p.getProductId(), p.getProductName());
                }

                log.warn("[배치] 재고 부족 상품: {}개 (기준: {}개 이하)", lowStockProducts.size(), LOW_STOCK_THRESHOLD);
                for (Product p : lowStockProducts) {
                    log.warn("  - [재고부족] ID:{}, 상품명:{}, 재고:{}개",
                            p.getProductId(), p.getProductName(), p.getProductStock());
                }
            }

        } catch (Exception e) {
            log.error("[배치] 재고 부족 알림 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 재고 부족 알림 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String checkLowStockProductsManual() {
        log.info("[배치] 재고 부족 알림 수동 실행");

        List<Product> lowStockProducts = productRepository.findLowStockProducts(LOW_STOCK_THRESHOLD);
        List<Product> outOfStockProducts = productRepository.findOutOfStockProducts();

        StringBuilder result = new StringBuilder();
        result.append("품절 상품: ").append(outOfStockProducts.size()).append("개, ");
        result.append("재고 부족 상품: ").append(lowStockProducts.size()).append("개");

        log.info("[배치] {}", result);
        return result.toString();
    }
}