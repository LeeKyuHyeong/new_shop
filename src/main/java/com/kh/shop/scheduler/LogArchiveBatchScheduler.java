package com.kh.shop.scheduler;

import com.kh.shop.repository.BatchLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogArchiveBatchScheduler {

    private final BatchLogRepository batchLogRepository;

    // 로그 보관 기간 (30일)
    private static final int LOG_RETENTION_DAYS = 30;

    /**
     * 매주 일요일 03:00에 30일 이상 된 로그 아카이브(삭제) 처리
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void archiveOldLogs() {
        log.info("========== [배치] 로그 아카이브 시작 ==========");

        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);

            // 오래된 배치 로그 삭제
            int deletedCount = batchLogRepository.deleteByStartedAtBefore(cutoffDate);

            log.info("[배치] {}개의 오래된 로그가 아카이브(삭제)되었습니다. (기준: {}일 이상)",
                    deletedCount, LOG_RETENTION_DAYS);

        } catch (Exception e) {
            log.error("[배치] 로그 아카이브 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 로그 아카이브 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional
    public String archiveOldLogsManual() {
        log.info("[배치] 로그 아카이브 수동 실행");

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
        int deletedCount = batchLogRepository.deleteByStartedAtBefore(cutoffDate);

        String result = deletedCount + "개의 오래된 로그가 아카이브(삭제)되었습니다.";
        log.info("[배치] {}", result);
        return result;
    }

    /**
     * 아카이브 대상 로그 개수 조회
     */
    @Transactional(readOnly = true)
    public int countOldLogs() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(LOG_RETENTION_DAYS);
        return batchLogRepository.countByStartedAtBefore(cutoffDate);
    }
}