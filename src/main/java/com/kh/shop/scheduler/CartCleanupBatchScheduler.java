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

    // 방치 기준 일수 (7일)
    private static final int ABANDONED_DAYS = 7;

    /**
     * 매일 03:00에 7일 이상 방치된 장바구니 정리
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupAbandonedCarts() {
        log.info("========== [배치] 장바구니 정리 시작 ==========");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(ABANDONED_DAYS);

            // 7일 이상 방치된 장바구니 조회
            List<Cart> abandonedCarts = cartRepository.findByUpdatedDateBefore(threshold);

            if (abandonedCarts.isEmpty()) {
                log.info("[배치] 정리할 장바구니가 없습니다.");
            } else {
                int count = abandonedCarts.size();

                // 장바구니 삭제
                cartRepository.deleteAll(abandonedCarts);

                log.info("[배치] {}개의 방치된 장바구니가 삭제되었습니다. (기준: {}일 이상)", count, ABANDONED_DAYS);
            }

        } catch (Exception e) {
            log.error("[배치] 장바구니 정리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 장바구니 정리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     * @return 삭제된 장바구니 개수
     */
    @Transactional
    public int cleanupAbandonedCartsManual() {
        log.info("[배치] 장바구니 정리 수동 실행");

        LocalDateTime threshold = LocalDateTime.now().minusDays(ABANDONED_DAYS);
        List<Cart> abandonedCarts = cartRepository.findByUpdatedDateBefore(threshold);

        if (abandonedCarts.isEmpty()) {
            log.info("[배치] 정리할 장바구니가 없습니다.");
            return 0;
        }

        int count = abandonedCarts.size();
        cartRepository.deleteAll(abandonedCarts);

        log.info("[배치] {}개의 방치된 장바구니가 삭제되었습니다.", count);
        return count;
    }

    /**
     * 방치된 장바구니 개수 조회 (미리보기용)
     */
    @Transactional(readOnly = true)
    public int countAbandonedCarts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(ABANDONED_DAYS);
        return cartRepository.findByUpdatedDateBefore(threshold).size();
    }
}