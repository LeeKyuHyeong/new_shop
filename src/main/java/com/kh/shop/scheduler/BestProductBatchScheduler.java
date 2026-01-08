package com.kh.shop.scheduler;

import com.kh.shop.entity.Product;
import com.kh.shop.repository.OrderRepository;
import com.kh.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BestProductBatchScheduler {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // 베스트 상품 집계 기간 (30일)
    private static final int PERIOD_DAYS = 30;
    // 베스트 상품 최대 순위
    private static final int MAX_RANK = 10;

    /**
     * 매일 00:00에 최근 30일 주문량 기반 베스트 상품 순위 갱신
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateBestProducts() {
        log.info("========== [배치] 베스트 상품 갱신 시작 ==========");

        try {
            // 기존 베스트 순위 초기화
            productRepository.clearAllBestRank();

            // 최근 30일간 판매량 조회
            LocalDateTime startDate = LocalDateTime.now().minusDays(PERIOD_DAYS);
            List<Object[]> salesData = orderRepository.findProductSalesCount(startDate);

            if (salesData.isEmpty()) {
                log.info("[배치] 최근 {}일간 판매 데이터가 없습니다.", PERIOD_DAYS);
            } else {
                int rank = 1;
                int updatedCount = 0;

                for (Object[] data : salesData) {
                    if (rank > MAX_RANK) break;

                    Long productId = (Long) data[0];
                    Long salesCount = (Long) data[1];

                    Optional<Product> productOpt = productRepository.findById(productId);
                    if (productOpt.isPresent()) {
                        Product product = productOpt.get();
                        product.setBestRank(rank);
                        product.setSalesCount(salesCount.intValue());
                        productRepository.save(product);

                        log.debug("[배치] 베스트 {}위: {} (판매량: {})", rank, product.getProductName(), salesCount);
                        rank++;
                        updatedCount++;
                    }
                }

                log.info("[배치] 베스트 상품 {}개가 갱신되었습니다. (기준: 최근 {}일)", updatedCount, PERIOD_DAYS);
            }

        } catch (Exception e) {
            log.error("[배치] 베스트 상품 갱신 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 베스트 상품 갱신 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     * @return 갱신된 베스트 상품 개수
     */
    @Transactional
    public int updateBestProductsManual() {
        log.info("[배치] 베스트 상품 갱신 수동 실행");

        // 기존 베스트 순위 초기화
        productRepository.clearAllBestRank();

        // 최근 30일간 판매량 조회
        LocalDateTime startDate = LocalDateTime.now().minusDays(PERIOD_DAYS);
        List<Object[]> salesData = orderRepository.findProductSalesCount(startDate);

        if (salesData.isEmpty()) {
            log.info("[배치] 최근 {}일간 판매 데이터가 없습니다.", PERIOD_DAYS);
            return 0;
        }

        int rank = 1;
        int updatedCount = 0;

        for (Object[] data : salesData) {
            if (rank > MAX_RANK) break;

            Long productId = (Long) data[0];
            Long salesCount = (Long) data[1];

            Optional<Product> productOpt = productRepository.findById(productId);
            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                product.setBestRank(rank);
                product.setSalesCount(salesCount.intValue());
                productRepository.save(product);
                rank++;
                updatedCount++;
            }
        }

        log.info("[배치] 베스트 상품 {}개가 갱신되었습니다.", updatedCount);
        return updatedCount;
    }

    /**
     * 현재 베스트 상품 개수 조회
     */
    @Transactional(readOnly = true)
    public int countBestProducts() {
        return productRepository.findBestRankedProducts("Y").size();
    }
}