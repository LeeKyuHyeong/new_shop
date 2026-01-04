package com.kh.shop.controller.admin;

import com.kh.shop.service.BatchService;
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
        Map<String, Object> result = batchService.executeBatch(batchId);
        return ResponseEntity.ok(result);
    }
}