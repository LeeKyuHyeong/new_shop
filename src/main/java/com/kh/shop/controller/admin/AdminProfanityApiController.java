package com.kh.shop.controller.admin;

import com.kh.shop.entity.ProfanityWord;
import com.kh.shop.service.ProfanityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/profanity")
@RequiredArgsConstructor
public class AdminProfanityApiController {

    private final ProfanityService profanityService;

    /**
     * 비속어 추가
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addWord(@RequestBody Map<String, String> request,
                                                       HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            String word = request.get("word");
            String category = request.get("category");
            String description = request.get("description");
            String createdBy = (String) session.getAttribute("loggedInUser");

            if (word == null || word.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "단어를 입력해주세요.");
                return ResponseEntity.badRequest().body(result);
            }

            ProfanityWord saved = profanityService.addWord(word.trim(), category, description, createdBy);

            result.put("success", true);
            result.put("message", "단어가 추가되었습니다.");
            result.put("data", Map.of(
                    "id", saved.getId(),
                    "word", saved.getWord(),
                    "category", saved.getCategory() != null ? saved.getCategory() : ""
            ));
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 비속어 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateWord(@PathVariable Long id,
                                                          @RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String word = (String) request.get("word");
            String category = (String) request.get("category");
            String description = (String) request.get("description");
            Boolean isActive = (Boolean) request.get("isActive");

            if (word == null || word.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "단어를 입력해주세요.");
                return ResponseEntity.badRequest().body(result);
            }

            ProfanityWord updated = profanityService.updateWord(id, word.trim(), category, description, isActive);

            result.put("success", true);
            result.put("message", "단어가 수정되었습니다.");
            result.put("data", Map.of(
                    "id", updated.getId(),
                    "word", updated.getWord(),
                    "category", updated.getCategory() != null ? updated.getCategory() : "",
                    "isActive", updated.getIsActive()
            ));
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 비속어 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteWord(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            profanityService.deleteWord(id);
            result.put("success", true);
            result.put("message", "단어가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 활성화/비활성화 토글
     */
    @PostMapping("/{id}/toggle")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProfanityWord updated = profanityService.toggleActive(id);
            result.put("success", true);
            result.put("message", updated.getIsActive() ? "활성화되었습니다." : "비활성화되었습니다.");
            result.put("isActive", updated.getIsActive());
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 일괄 삭제
     */
    @PostMapping("/delete-multiple")
    public ResponseEntity<Map<String, Object>> deleteMultiple(@RequestBody Map<String, List<Long>> request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = request.get("ids");
            if (ids == null || ids.isEmpty()) {
                result.put("success", false);
                result.put("message", "삭제할 항목을 선택해주세요.");
                return ResponseEntity.badRequest().body(result);
            }

            int count = profanityService.deleteMultiple(ids);
            result.put("success", true);
            result.put("message", count + "개 단어가 삭제되었습니다.");
            result.put("deletedCount", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 일괄 추가
     */
    @PostMapping("/add-multiple")
    public ResponseEntity<Map<String, Object>> addMultiple(@RequestBody Map<String, Object> request,
                                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<String> words = (List<String>) request.get("words");
            String category = (String) request.get("category");
            String createdBy = (String) session.getAttribute("loggedInUser");

            if (words == null || words.isEmpty()) {
                result.put("success", false);
                result.put("message", "추가할 단어를 입력해주세요.");
                return ResponseEntity.badRequest().body(result);
            }

            int count = profanityService.addMultiple(words, category, createdBy);
            result.put("success", true);
            result.put("message", count + "개 단어가 추가되었습니다.");
            result.put("addedCount", count);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 캐시 갱신
     */
    @PostMapping("/refresh-cache")
    public ResponseEntity<Map<String, Object>> refreshCache() {
        Map<String, Object> result = new HashMap<>();
        try {
            profanityService.refreshCache();
            result.put("success", true);
            result.put("message", "캐시가 갱신되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "캐시 갱신 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 기본 데이터 초기화
     */
    @PostMapping("/initialize")
    public ResponseEntity<Map<String, Object>> initialize() {
        Map<String, Object> result = new HashMap<>();
        try {
            int count = profanityService.initializeDefaultWords();
            if (count > 0) {
                result.put("success", true);
                result.put("message", count + "개 기본 단어가 초기화되었습니다.");
            } else {
                result.put("success", false);
                result.put("message", "이미 데이터가 존재합니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "초기화 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(result);
        }
        return ResponseEntity.ok(result);
    }

    /**
     * 통계 조회
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(profanityService.getStats());
    }

    /**
     * 테스트 검증
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> test(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(profanityService.validate(text));
    }
}