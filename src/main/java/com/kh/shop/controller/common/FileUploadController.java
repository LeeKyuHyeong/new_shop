package com.kh.shop.controller.common;

import com.kh.shop.security.FileUploadValidator;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Autowired
    private FileUploadValidator fileUploadValidator;

    // 에디터 이미지 업로드 (관리자 상품 등록/수정 페이지의 리치 텍스트 에디터용).
    // 검증: ADMIN 권한 + FileUploadValidator (확장자 화이트리스트 + MIME + 매직 바이트).
    @PostMapping("/editor-image")
    public ResponseEntity<Map<String, Object>> uploadEditorImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 권한 체크 - 에디터 이미지는 관리자만 업로드 가능
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.status(403).body(response);
        }

        return doUpload(file, "editor", response);
    }

    // 리뷰 이미지 업로드 (작성 도중 임시 업로드 - 로그인 필수).
    @PostMapping("/review-image")
    public ResponseEntity<Map<String, Object>> uploadReviewImage(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        // 권한 체크 - 로그인 사용자만
        Object loggedInUser = session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }

        return doUpload(file, "reviews/temp", response);
    }

    private ResponseEntity<Map<String, Object>> doUpload(MultipartFile file,
                                                          String subDir,
                                                          Map<String, Object> response) {
        try {
            // 보안 검증 - 확장자/MIME/매직 바이트/경로 조작 모두 체크
            FileUploadValidator.ValidationResult result = fileUploadValidator.validateImage(file);
            if (!result.isValid()) {
                response.put("success", false);
                response.put("message", result.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // 안전한 확장자만 사용 (원본 파일명 사용하지 않음)
            String safeExtension = fileUploadValidator.getSafeExtension(file.getOriginalFilename());
            if (safeExtension.isEmpty()) {
                response.put("success", false);
                response.put("message", "허용되지 않는 파일 형식입니다.");
                return ResponseEntity.badRequest().body(response);
            }

            String targetDir = uploadDir + "/" + subDir;
            Path uploadPath = Paths.get(targetDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String savedName = UUID.randomUUID().toString() + safeExtension;
            Path filePath = uploadPath.resolve(savedName);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/uploads/" + subDir + "/" + savedName;
            response.put("success", true);
            response.put("url", imageUrl);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("[UPLOAD] 파일 업로드 실패: subDir={}", subDir, e);
            response.put("success", false);
            response.put("message", "파일 업로드 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
