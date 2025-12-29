package com.kh.shop.controller.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // 에디터 이미지 업로드
    @PostMapping("/editor-image")
    public ResponseEntity<Map<String, Object>> uploadEditorImage(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 이미지 타입 체크
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.ok(response);
            }

            // 파일 크기 체크 (10MB)
            if (file.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 10MB 이하만 가능합니다.");
                return ResponseEntity.ok(response);
            }

            // 저장 경로 생성
            String editorUploadDir = uploadDir + "/editor";
            Path uploadPath = Paths.get(editorUploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명 생성
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/uploads/editor/" + savedName;

            response.put("success", true);
            response.put("url", imageUrl);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }

    // 리뷰 이미지 업로드
    @PostMapping("/review-image")
    public ResponseEntity<Map<String, Object>> uploadReviewImage(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 이미지 타입 체크
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.ok(response);
            }

            // 파일 크기 체크 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "파일 크기는 5MB 이하만 가능합니다.");
                return ResponseEntity.ok(response);
            }

            // 저장 경로 생성
            String reviewUploadDir = uploadDir + "/reviews/temp";
            Path uploadPath = Paths.get(reviewUploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 파일명 생성
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(originalName.lastIndexOf("."));
            String savedName = UUID.randomUUID().toString() + extension;

            // 파일 저장
            Path filePath = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/uploads/reviews/temp/" + savedName;

            response.put("success", true);
            response.put("url", imageUrl);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
        }

        return ResponseEntity.ok(response);
    }
}