package com.kh.shop.scheduler;

import com.kh.shop.entity.BatchLog;
import com.kh.shop.entity.Product;
import com.kh.shop.repository.BatchLogRepository;
import com.kh.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 기본 이미지 상품 목록 추출 배치 스케줄러
 *
 * - 매일 06:00 실행
 * - thumbnail_url이 기본 이미지인 상품 목록을 txt 파일로 저장
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductImageBatchScheduler {

    private final ProductRepository productRepository;
    private final BatchLogRepository batchLogRepository;

    @Value("${batch.export.dir:./list}")
    private String outputDir;
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 매일 06:00 실행 - 기본 이미지 상품 목록 추출
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void exportDefaultImageProducts() {
        executeExport("SCHEDULED_06:00");
    }

    /**
     * 기본 이미지 상품 목록 추출 실행 (공통 로직)
     */
    private String executeExport(String triggeredBy) {
        log.info("[이미지배치] ========== 기본 이미지 상품 목록 추출 시작 ({}) ==========", triggeredBy);

        long startTime = System.currentTimeMillis();
        BatchLog logEntry = BatchLog.builder()
                .batchId("PRODUCT_IMAGE_GENERATE")
                .batchName("기본 이미지 상품 목록 추출")
                .status("RUNNING")
                .triggeredBy(triggeredBy)
                .startedAt(LocalDateTime.now())
                .build();
        batchLogRepository.save(logEntry);

        try {
            // 기본 이미지인 상품 조회
            List<Product> products = productRepository.findProductsWithoutImagesAll();
            log.info("[이미지배치] 기본 이미지 상품 수: {}개", products.size());

            if (products.isEmpty()) {
                String message = "기본 이미지인 상품이 없습니다.";
                log.info("[이미지배치] {}", message);

                logEntry.setStatus("SUCCESS");
                logEntry.setMessage(message);
                logEntry.setFinishedAt(LocalDateTime.now());
                logEntry.setExecutionTime(System.currentTimeMillis() - startTime);
                batchLogRepository.save(logEntry);
                return message;
            }

            // 파일 저장
            String fileName = createExportFile(products);

            String message = String.format("기본 이미지 상품 %d개 목록 저장 완료 (%s)", products.size(), fileName);
            log.info("[이미지배치] {}", message);

            // 로그 저장
            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("SUCCESS");
            logEntry.setMessage(message);
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);

            log.info("[이미지배치] ========== 기본 이미지 상품 목록 추출 종료 ==========");
            return message;

        } catch (Exception e) {
            log.error("[이미지배치] 배치 실행 중 오류 발생", e);

            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("FAILED");
            logEntry.setMessage("오류: " + e.getMessage());
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);

            return "오류 발생: " + e.getMessage();
        }
    }

    /**
     * 상품 목록을 txt 파일로 저장
     */
    private String createExportFile(List<Product> products) throws IOException {
        // 디렉토리 생성
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 파일명 생성 (default_image_products_20260111_060000.txt)
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fileName = "default_image_products_" + timestamp + ".txt";
        File file = new File(outputDir, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 헤더
            writer.write("=== 기본 이미지 상품 목록 ===");
            writer.newLine();
            writer.write("추출일시: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.newLine();
            writer.write("총 상품 수: " + products.size() + "개");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            // 상품 목록
            writer.write(String.format("%-10s | %s", "상품ID", "상품명"));
            writer.newLine();
            writer.write("----------------------------------------");
            writer.newLine();

            for (Product product : products) {
                writer.write(String.format("%-10d | %s", product.getProductId(), product.getProductName()));
                writer.newLine();
            }

            writer.newLine();
            writer.write("=== 목록 끝 ===");
            writer.newLine();
        }

        log.info("[이미지배치] 파일 저장 완료: {}", file.getAbsolutePath());
        return fileName;
    }

    /**
     * 수동 실행 - 기본 이미지 상품 목록 추출
     */
    public String generateAllMissingImagesManual() {
        return executeExport("MANUAL");
    }

    /**
     * 내보내기 파일 목록 조회 (최신순)
     */
    public List<String> getExportFileList() {
        File dir = new File(outputDir);
        if (!dir.exists() || !dir.isDirectory()) {
            return Collections.emptyList();
        }

        String[] files = dir.list((d, name) -> name.startsWith("default_image_products_") && name.endsWith(".txt"));
        if (files == null || files.length == 0) {
            return Collections.emptyList();
        }

        // 최신순 정렬
        Arrays.sort(files, Collections.reverseOrder());
        return Arrays.asList(files);
    }

    /**
     * 파일 객체 반환 (다운로드용)
     */
    public File getExportFile(String fileName) {
        // 보안: 파일명에 경로 조작 문자가 있으면 거부
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            return null;
        }

        File file = new File(outputDir, fileName);
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    /**
     * 내보내기 디렉토리 경로 반환
     */
    public String getOutputDir() {
        return outputDir;
    }
}
