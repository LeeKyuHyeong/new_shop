package com.kh.shop.scheduler;

import com.kh.shop.entity.Product;
import com.kh.shop.entity.Wishlist;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class WishlistPriceAlertBatchScheduler {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    /**
     * 매일 08:00에 찜한 상품 할인 시작 알림
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional(readOnly = true)
    public void sendWishlistPriceAlerts() {
        log.info("========== [배치] 위시리스트 가격 알림 시작 ==========");

        try {
            // 최근 24시간 내 할인이 적용된 상품 조회
            LocalDateTime since = LocalDateTime.now().minusHours(24);
            List<Product> discountedProducts = productRepository.findRecentlyDiscountedProducts(since);

            if (discountedProducts.isEmpty()) {
                log.info("[배치] 최근 할인 시작된 상품이 없습니다.");
                return;
            }

            int alertCount = 0;
            Map<String, List<String>> userAlerts = new HashMap<>();

            for (Product product : discountedProducts) {
                // 해당 상품을 찜한 사용자들 조회
                long wishCount = wishlistRepository.countByProduct(product);

                if (wishCount > 0) {
                    log.info("[배치] 할인 상품 알림 대상 - 상품:{}, 할인율:{}%, 찜한 사용자:{}명",
                            product.getProductName(),
                            product.getProductDiscount(),
                            wishCount);
                    alertCount++;
                }
            }

            log.info("[배치] 위시리스트 가격 알림 완료: 할인 상품 {}개에 대한 알림 처리", alertCount);

        } catch (Exception e) {
            log.error("[배치] 위시리스트 가격 알림 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 위시리스트 가격 알림 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String sendWishlistPriceAlertsManual() {
        log.info("[배치] 위시리스트 가격 알림 수동 실행");

        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<Product> discountedProducts = productRepository.findRecentlyDiscountedProducts(since);

        int alertCount = 0;
        for (Product product : discountedProducts) {
            long wishCount = wishlistRepository.countByProduct(product);
            if (wishCount > 0) {
                alertCount++;
            }
        }

        String result = "할인 알림 대상 상품: " + discountedProducts.size() + "개, 알림 발송: " + alertCount + "건";
        log.info("[배치] {}", result);
        return result;
    }
}