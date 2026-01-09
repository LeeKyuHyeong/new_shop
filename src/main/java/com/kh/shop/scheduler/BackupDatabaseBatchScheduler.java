package com.kh.shop.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class BackupDatabaseBatchScheduler {

    @Value("${spring.datasource.url:jdbc:mariadb://localhost:3306/kh_shop}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUsername;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${backup.dir:backups}")
    private String backupDir;

    // 백업 보관 기간 (7일)
    private static final int BACKUP_RETENTION_DAYS = 7;

    /**
     * 매일 05:00에 DB 자동 백업 및 오래된 백업 정리
     */
    @Scheduled(cron = "0 0 5 * * *")
    public void backupDatabase() {
        log.info("========== [배치] 데이터베이스 백업 시작 ==========");

        try {
            // 백업 디렉토리 생성
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            // 백업 파일명 생성
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "kh_shop_backup_" + timestamp + ".sql";
            Path backupFile = backupPath.resolve(backupFileName);

            // DB 이름 추출
            String dbName = extractDbName(datasourceUrl);

            // mysqldump 명령 실행
            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--single-transaction",
                    "--routines",
                    "--triggers",
                    dbName
            );
            pb.redirectOutput(backupFile.toFile());
            pb.redirectErrorStream(false);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long fileSize = Files.size(backupFile);
                log.info("[배치] DB 백업 완료 - 파일: {}, 크기: {}MB",
                        backupFileName,
                        String.format("%.2f", fileSize / (1024.0 * 1024.0)));

                // 오래된 백업 파일 정리
                cleanupOldBackups(backupPath);
            } else {
                log.error("[배치] DB 백업 실패 - 종료 코드: {}", exitCode);
            }

        } catch (Exception e) {
            log.error("[배치] 데이터베이스 백업 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 데이터베이스 백업 종료 ==========");
    }

    /**
     * 오래된 백업 파일 정리
     */
    private void cleanupOldBackups(Path backupPath) throws IOException {
        LocalDate cutoffDate = LocalDate.now().minusDays(BACKUP_RETENTION_DAYS);
        int deletedCount = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(backupPath, "*.sql")) {
            for (Path file : stream) {
                if (Files.getLastModifiedTime(file).toInstant()
                        .isBefore(cutoffDate.atStartOfDay().toInstant(java.time.ZoneOffset.UTC))) {
                    Files.delete(file);
                    deletedCount++;
                    log.debug("[배치] 오래된 백업 삭제: {}", file.getFileName());
                }
            }
        }

        if (deletedCount > 0) {
            log.info("[배치] {}개의 오래된 백업 파일 삭제 (기준: {}일 이상)", deletedCount, BACKUP_RETENTION_DAYS);
        }
    }

    /**
     * JDBC URL에서 DB 이름 추출
     */
    private String extractDbName(String jdbcUrl) {
        // jdbc:mariadb://localhost:3306/kh_shop
        int lastSlash = jdbcUrl.lastIndexOf('/');
        int queryStart = jdbcUrl.indexOf('?');
        if (queryStart > 0) {
            return jdbcUrl.substring(lastSlash + 1, queryStart);
        }
        return jdbcUrl.substring(lastSlash + 1);
    }

    /**
     * 수동 실행용 메서드
     */
    public String backupDatabaseManual() {
        log.info("[배치] 데이터베이스 백업 수동 실행");

        try {
            Path backupPath = Paths.get(backupDir);
            if (!Files.exists(backupPath)) {
                Files.createDirectories(backupPath);
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "kh_shop_backup_" + timestamp + ".sql";
            Path backupFile = backupPath.resolve(backupFileName);

            String dbName = extractDbName(datasourceUrl);

            ProcessBuilder pb = new ProcessBuilder(
                    "mysqldump",
                    "-u" + dbUsername,
                    "-p" + dbPassword,
                    "--single-transaction",
                    dbName
            );
            pb.redirectOutput(backupFile.toFile());

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                long fileSize = Files.size(backupFile);
                String result = "백업 완료: " + backupFileName + " (" +
                        String.format("%.2f", fileSize / (1024.0 * 1024.0)) + "MB)";
                log.info("[배치] {}", result);
                return result;
            } else {
                return "백업 실패 (종료 코드: " + exitCode + ")";
            }

        } catch (Exception e) {
            log.error("[배치] DB 백업 실패: {}", e.getMessage(), e);
            return "백업 실패: " + e.getMessage();
        }
    }
}