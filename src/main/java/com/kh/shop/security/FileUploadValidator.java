package com.kh.shop.security;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Set;

/**
 * 파일 업로드 보안 검증기
 * - 허용 확장자 화이트리스트 검증
 * - MIME 타입 검증
 * - 파일 시그니처(매직넘버) 검증
 * - 파일 크기 검증
 */
@Component
public class FileUploadValidator {

    // 허용되는 이미지 확장자
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    // 허용되는 문서 확장자
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "txt"
    );

    // 허용되는 MIME 타입
    private static final Set<String> ALLOWED_IMAGE_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    // 이미지 파일 시그니처 (매직 넘버)
    private static final byte[] JPEG_SIGNATURE = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_SIGNATURE = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] GIF_SIGNATURE = new byte[]{0x47, 0x49, 0x46};
    private static final byte[] WEBP_SIGNATURE = new byte[]{0x52, 0x49, 0x46, 0x46}; // RIFF
    private static final byte[] BMP_SIGNATURE = new byte[]{0x42, 0x4D}; // BM

    // 최대 파일 크기 (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    /**
     * 이미지 파일 검증
     */
    public ValidationResult validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ValidationResult.error("파일이 비어있습니다.");
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_FILE_SIZE) {
            return ValidationResult.error("파일 크기가 10MB를 초과합니다.");
        }

        // 원본 파일명에서 확장자 추출
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ValidationResult.error("파일 확장자가 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 확장자 화이트리스트 검증
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return ValidationResult.error("허용되지 않는 파일 형식입니다. (허용: jpg, jpeg, png, gif, webp, bmp)");
        }

        // MIME 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_MIME_TYPES.contains(contentType.toLowerCase())) {
            return ValidationResult.error("허용되지 않는 MIME 타입입니다.");
        }

        // 파일 시그니처 검증
        try {
            if (!validateFileSignature(file, extension)) {
                return ValidationResult.error("파일 내용이 확장자와 일치하지 않습니다.");
            }
        } catch (IOException e) {
            return ValidationResult.error("파일 검증 중 오류가 발생했습니다.");
        }

        // 파일명 보안 검증 (경로 조작 방지)
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            return ValidationResult.error("잘못된 파일명입니다.");
        }

        return ValidationResult.success();
    }

    /**
     * 파일 시그니처(매직 넘버) 검증
     */
    private boolean validateFileSignature(MultipartFile file, String extension) throws IOException {
        byte[] header = new byte[12];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 2) {
                return false;
            }
        }

        switch (extension.toLowerCase()) {
            case "jpg":
            case "jpeg":
                return startsWith(header, JPEG_SIGNATURE);
            case "png":
                return startsWith(header, PNG_SIGNATURE);
            case "gif":
                return startsWith(header, GIF_SIGNATURE);
            case "webp":
                // WEBP: RIFF....WEBP
                return startsWith(header, WEBP_SIGNATURE) &&
                       header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
            case "bmp":
                return startsWith(header, BMP_SIGNATURE);
            default:
                return false;
        }
    }

    private boolean startsWith(byte[] data, byte[] signature) {
        if (data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 안전한 파일명 생성 (확장자만 유지)
     */
    public String getSafeExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

        // 허용된 확장자만 반환
        if (ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            return "." + extension;
        }
        return "";
    }

    /**
     * 검증 결과 클래스
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
