package com.kh.shop.scheduler;

import com.kh.shop.entity.Cart;
import com.kh.shop.repository.CartRepository;
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
public class CartCleanupBatchScheduler {

    private final CartRepository cartRepository;

    /**
     * 매일 03:00에 7일 이상 방치된 장바구니 정리
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldCarts() {
        log.info("========== [배치] 장바구니 정리 시작 ==========");

        try {
            // 7일 이전 날짜 계산
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            // 7일 이상 방치된 장바구니 조회
            List<Cart> oldCarts = cartRepository.findOldCarts(sevenDaysAgo, "Y");

            if (oldCarts.isEmpty()) {
                log.info("[배치] 정리할 장바구니가 없습니다.");
                log.info("========== [배치] 장바구니 정리 종료 ==========");
                return;
            }

            // 장바구니 항목 비활성화 처리
            int cleanedCount = 0;
            for (Cart cart : oldCarts) {
                cart.setUseYn("N");
                cartRepository.save(cart);
                cleanedCount++;
            }

            log.info("[배치] 장바구니 정리 완료 - 총 {}개 항목 정리됨", cleanedCount);

        } catch (Exception e) {
            log.error("[배치] 장바구니 정리 실패: {}", e.getMessage(), e);
            throw e;
        }

        log.info("========== [배치] 장바구니 정리 종료 ==========");
    }
}
