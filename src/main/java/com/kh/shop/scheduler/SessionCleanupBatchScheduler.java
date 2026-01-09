package com.kh.shop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupBatchScheduler {

    // 세션 만료 시간 (2시간)
    private static final int SESSION_TIMEOUT_HOURS = 2;

    /**
     * 매시 00분에 만료된 세션 데이터 정리
     * (Spring Session을 사용하는 경우 DB에서 정리, 기본은 파일 세션 정리)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void cleanupExpiredSessions() {
        log.info("========== [배치] 세션 정리 시작 ==========");

        try {
            // 임시 세션 파일 정리 (파일 기반 세션 사용 시)
            Path sessionPath = Paths.get(System.getProperty("java.io.tmpdir"), "tomcat-sessions");

            if (Files.exists(sessionPath)) {
                Instant cutoffTime = Instant.now().minus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS);
                int[] cleanedCount = {0};

                Files.list(sessionPath).forEach(file -> {
                    try {
                        if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoffTime)) {
                            Files.delete(file);
                            cleanedCount[0]++;
                        }
                    } catch (Exception e) {
                        log.warn("세션 파일 삭제 실패: {}", file);
                    }
                });

                log.info("[배치] 만료된 세션 {}개 정리 완료", cleanedCount[0]);
            } else {
                // Spring Security / Spring Session 사용 시 자동 관리됨
                log.info("[배치] 세션 정리 완료 (Spring Session에서 자동 관리)");
            }

        } catch (Exception e) {
            log.error("[배치] 세션 정리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 세션 정리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    public String cleanupExpiredSessionsManual() {
        log.info("[배치] 세션 정리 수동 실행");

        try {
            Path sessionPath = Paths.get(System.getProperty("java.io.tmpdir"), "tomcat-sessions");

            if (Files.exists(sessionPath)) {
                Instant cutoffTime = Instant.now().minus(SESSION_TIMEOUT_HOURS, ChronoUnit.HOURS);
                int[] cleanedCount = {0};

                Files.list(sessionPath).forEach(file -> {
                    try {
                        if (Files.getLastModifiedTime(file).toInstant().isBefore(cutoffTime)) {
                            Files.delete(file);
                            cleanedCount[0]++;
                        }
                    } catch (Exception e) {
                        log.warn("세션 파일 삭제 실패: {}", file);
                    }
                });

                String result = cleanedCount[0] + "개의 만료된 세션이 정리되었습니다.";
                log.info("[배치] {}", result);
                return result;
            }

            return "세션 정리 완료 (Spring Session 자동 관리)";

        } catch (Exception e) {
            log.error("[배치] 세션 정리 실패: {}", e.getMessage(), e);
            return "세션 정리 실패: " + e.getMessage();
        }
    }
}