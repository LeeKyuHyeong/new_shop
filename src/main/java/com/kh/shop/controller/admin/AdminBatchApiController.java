package com.kh.shop.controller.admin;

import com.kh.shop.scheduler.ProductImageBatchScheduler;
import com.kh.shop.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
     * 내보내기 파일 목록 조회
     */
    @GetMapping("/export/files")
    public ResponseEntity<List<String>> getExportFiles() {
        List<String> files = productImageBatchScheduler.getExportFileList();
        return ResponseEntity.ok(files);
    }

    /**
     * 내보내기 파일 다운로드
     */
    @GetMapping("/export/download/{fileName}")
    public ResponseEntity<Resource> downloadExportFile(@PathVariable String fileName) {
        File file = productImageBatchScheduler.getExportFile(fileName);

        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
