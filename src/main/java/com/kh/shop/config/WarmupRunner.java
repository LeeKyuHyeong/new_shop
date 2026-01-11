package com.kh.shop.config;

import com.kh.shop.service.CategoryService;
import com.kh.shop.service.ProductService;
import com.kh.shop.service.SlideService;
import com.kh.shop.service.PopupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 주요 서비스를 미리 호출하여 워밍업
 * - JVM 클래스 로딩
 * - Hibernate 엔티티 메타데이터 초기화
 * - DB 커넥션 풀 초기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WarmupRunner implements ApplicationRunner {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final SlideService slideService;
    private final PopupService popupService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("=== Application Warmup Start ===");
        long startTime = System.currentTimeMillis();

        try {
            // 카테고리 메뉴 워밍업
            categoryService.getParentCategoriesWithChildren();
            log.info("Category warmup completed");

            // 메인 페이지 상품 워밍업
            productService.getNewProducts(8);
            productService.getBestProducts(8);
            productService.getDiscountProducts(8);
            log.info("Product warmup completed");

            // 슬라이드 워밍업
            slideService.getActiveSlides();
            log.info("Slide warmup completed");

            // 팝업 워밍업
            popupService.getActivePopups();
            log.info("Popup warmup completed");

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("=== Application Warmup Completed in {}ms ===", elapsed);

        } catch (Exception e) {
            log.warn("Application warmup failed: {}", e.getMessage());
        }
    }
}
