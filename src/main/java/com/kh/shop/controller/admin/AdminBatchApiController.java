package com.kh.shop.controller.admin;

import com.kh.shop.service.BatchService;
import com.kh.shop.scheduler.ProductImageBatchScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/batch")
@RequiredArgsConstructor
public class AdminBatchApiController {

    private final BatchService batchService;
    private final ProductImageBatchScheduler productImageBatchScheduler;

    /**
     * 배치 목록 조회
     */
    @GetMapping("")
    public ResponseEntity<List<Map<String, Object>>> getBatchList() {
        return ResponseEntity.ok(batchService.getAllBatchInfo());
    }

    /**
     * 배치 수동 실행
     */
    @PostMapping("/{batchId}/execute")
    public ResponseEntity<Map<String, Object>> executeBatch(@PathVariable String batchId) {
        Map<String, Object> result = batchService.executeBatch(batchId, "MANUAL");
        return ResponseEntity.ok(result);
    }

    /**
     * 이미지 생성 일일 사용량 조회
     */
    @GetMapping("/image-usage")
    public ResponseEntity<Map<String, Object>> getImageUsageStatus() {
        return ResponseEntity.ok(productImageBatchScheduler.getUsageStatus());
    }

    /**
     * 이미지 생성 일일 카운터 리셋
     */
    @PostMapping("/image-usage/reset")
    public ResponseEntity<Map<String, Object>> resetImageUsageCounter() {
        return ResponseEntity.ok(productImageBatchScheduler.resetDailyCounter());
    }
}