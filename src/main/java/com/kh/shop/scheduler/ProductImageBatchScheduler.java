package com.kh.shop.scheduler;

import com.kh.shop.entity.BatchLog;
import com.kh.shop.repository.BatchLogRepository;
import com.kh.shop.service.ImageGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 상품 이미지 자동 생성 배치 스케줄러
 *
 * Google AI Studio 무료 티어 제한에 맞춤:
 * - 일일 최대 40장 (상품 10개 × 4장)
 * - 하루 3회 실행: 08:00, 14:00, 20:00
 * - 회당 최대 3개 상품 처리 (12장)
 * - 하루 총 9개 상품 = 36장 (여유분 4장)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductImageBatchScheduler {

    private final ImageGenerationService imageGenerationService;
    private final BatchLogRepository batchLogRepository;

    @Value("${image.batch.enabled:false}")
    private boolean batchEnabled;

    @Value("${image.batch.max-products-per-run:3}")
    private int maxProductsPerRun;

    /**
     * 오전 8시 실행 - 1차 이미지 생성
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void generateProductImagesMorning() {
        if (!batchEnabled) {
            log.info("[이미지배치] 배치가 비활성화되어 있습니다.");
            return;
        }
        executeImageGeneration("SCHEDULED_08:00");
    }

    /**
     * 오후 2시 실행 - 2차 이미지 생성
     */
    @Scheduled(cron = "0 0 14 * * *")
    public void generateProductImagesAfternoon() {
        if (!batchEnabled) {
            log.info("[이미지배치] 배치가 비활성화되어 있습니다.");
            return;
        }
        executeImageGeneration("SCHEDULED_14:00");
    }

    /**
     * 오후 8시 실행 - 3차 이미지 생성
     */
    @Scheduled(cron = "0 0 20 * * *")
    public void generateProductImagesEvening() {
        if (!batchEnabled) {
            log.info("[이미지배치] 배치가 비활성화되어 있습니다.");
            return;
        }
        executeImageGeneration("SCHEDULED_20:00");
    }

    /**
     * 이미지 생성 실행 (공통 로직)
     */
    private void executeImageGeneration(String triggeredBy) {
        log.info("[이미지배치] ========== 상품 이미지 생성 배치 시작 ({}) ==========", triggeredBy);

        long startTime = System.currentTimeMillis();
        BatchLog logEntry = BatchLog.builder()
                .batchId("PRODUCT_IMAGE_GENERATE")
                .batchName("상품 이미지 자동 생성")
                .status("RUNNING")
                .triggeredBy(triggeredBy)
                .startedAt(LocalDateTime.now())
                .build();
        batchLogRepository.save(logEntry);

        try {
            // 이미지 없는 상품 수 확인
            int productsWithoutImages = imageGenerationService.countProductsWithoutImages();
            log.info("[이미지배치] 이미지 없는 상품 수: {}개", productsWithoutImages);

            if (productsWithoutImages == 0) {
                String message = "이미지가 없는 상품이 없습니다.";
                log.info("[이미지배치] {}", message);

                logEntry.setStatus("SUCCESS");
                logEntry.setMessage(message);
                logEntry.setFinishedAt(LocalDateTime.now());
                logEntry.setExecutionTime(System.currentTimeMillis() - startTime);
                batchLogRepository.save(logEntry);
                return;
            }

            // 현재 일일 사용량 확인
            Map<String, Object> usageStatus = imageGenerationService.getDailyUsageStatus();
            log.info("[이미지배치] 일일 사용량: {}/{}",
                    usageStatus.get("dailyUsed"), usageStatus.get("dailyLimit"));

            // 이미지 생성 실행
            Map<String, Object> result = imageGenerationService.generateImagesForProductsWithoutImages(maxProductsPerRun);

            String message = (String) result.get("message");
            log.info("[이미지배치] 결과: {}", message);
            log.info("[이미지배치] 남은 일일 할당량: {}장", result.get("remainingQuota"));

            // 로그 저장
            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("SUCCESS");
            logEntry.setMessage(message + " (소요시간: " + executionTime + "ms)");
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);

        } catch (Exception e) {
            log.error("[이미지배치] 배치 실행 중 오류 발생", e);

            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("FAILED");
            logEntry.setMessage("오류: " + e.getMessage());
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);
        }

        log.info("[이미지배치] ========== 상품 이미지 생성 배치 종료 ==========");
    }

    /**
     * 수동 실행 - 모든 이미지 없는 상품 처리 (일일 제한 내)
     */
    public String generateAllMissingImagesManual() {
        log.info("[이미지배치] 수동 실행 시작 - 모든 이미지 없는 상품");

        Map<String, Object> result = imageGenerationService.generateImagesForProductsWithoutImages(10);

        StringBuilder sb = new StringBuilder();
        sb.append((String) result.get("message"));
        sb.append(" [일일 사용량: ").append(result.get("dailyUsed"));
        sb.append("/").append(result.get("dailyLimit")).append("]");

        return sb.toString();
    }

    /**
     * 수동 실행 - 특정 개수만 처리
     */
    public String generateImagesManual(int maxProducts) {
        log.info("[이미지배치] 수동 실행 시작 - 최대 {}개 상품", maxProducts);

        Map<String, Object> result = imageGenerationService.generateImagesForProductsWithoutImages(maxProducts);

        StringBuilder sb = new StringBuilder();
        sb.append((String) result.get("message"));
        sb.append(" [일일 사용량: ").append(result.get("dailyUsed"));
        sb.append("/").append(result.get("dailyLimit")).append("]");

        return sb.toString();
    }

    /**
     * 일일 사용량 상태 조회
     */
    public Map<String, Object> getUsageStatus() {
        return imageGenerationService.getDailyUsageStatus();
    }

    /**
     * 일일 카운터 강제 리셋
     */
    public Map<String, Object> resetDailyCounter() {
        return imageGenerationService.forceResetDailyCounter();
    }
}