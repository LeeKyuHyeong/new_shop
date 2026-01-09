package com.kh.shop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TempFileCleanupBatchScheduler {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // 임시 파일 보관 기간 (7일)
    private static final int TEMP_FILE_RETENTION_DAYS = 7;

    /**
     * 매일 04:00에 7일 이상 된 임시 업로드 파일 삭제
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanupTempFiles() {
        log.info("========== [배치] 임시 파일 정리 시작 ==========");

        try {
            Path tempPath = Paths.get(uploadDir, "temp");

            if (!Files.exists(tempPath)) {
                log.info("[배치] 임시 폴더가 존재하지 않습니다: {}", tempPath);
                return;
            }

            Instant cutoffTime = Instant.now().minus(TEMP_FILE_RETENTION_DAYS, ChronoUnit.DAYS);
            int[] deletedCount = {0};
            long[] deletedSize = {0};

            Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.creationTime().toInstant().isBefore(cutoffTime)) {
                        long fileSize = Files.size(file);
                        Files.delete(file);
                        deletedCount[0]++;
                        deletedSize[0] += fileSize;
                        log.debug("[배치] 삭제된 임시 파일: {}", file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // 빈 디렉토리 삭제
                    if (!dir.equals(tempPath)) {
                        File directory = dir.toFile();
                        String[] files = directory.list();
                        if (files != null && files.length == 0) {
                            Files.delete(dir);
                            log.debug("[배치] 빈 디렉토리 삭제: {}", dir);
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            log.info("[배치] 임시 파일 정리 완료 - 삭제 파일: {}개, 용량: {}MB",
                    deletedCount[0],
                    String.format("%.2f", deletedSize[0] / (1024.0 * 1024.0)));

        } catch (Exception e) {
            log.error("[배치] 임시 파일 정리 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 임시 파일 정리 종료 ==========");
    }

    /**
     * 수동 실행용 메서드
     */
    public String cleanupTempFilesManual() {
        log.info("[배치] 임시 파일 정리 수동 실행");

        try {
            Path tempPath = Paths.get(uploadDir, "temp");

            if (!Files.exists(tempPath)) {
                return "임시 폴더 없음";
            }

            Instant cutoffTime = Instant.now().minus(TEMP_FILE_RETENTION_DAYS, ChronoUnit.DAYS);
            int[] deletedCount = {0};

            Files.walkFileTree(tempPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.creationTime().toInstant().isBefore(cutoffTime)) {
                        Files.delete(file);
                        deletedCount[0]++;
                    }
                    return FileVisitResult.CONTINUE;
                }
            });

            String result = deletedCount[0] + "개의 임시 파일이 삭제되었습니다.";
            log.info("[배치] {}", result);
            return result;

        } catch (Exception e) {
            log.error("[배치] 임시 파일 정리 실패: {}", e.getMessage(), e);
            return "정리 실패: " + e.getMessage();
        }
    }
}