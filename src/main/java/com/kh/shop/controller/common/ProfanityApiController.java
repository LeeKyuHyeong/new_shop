package com.kh.shop.controller.common;

import com.kh.shop.service.ProfanityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profanity")
@RequiredArgsConstructor
public class ProfanityApiController {

    private final ProfanityService profanityService;

    /**
     * 텍스트 검증
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        return ResponseEntity.ok(profanityService.validate(text));
    }

    /**
     * 여러 필드 검증
     */
    @PostMapping("/validate-fields")
    public ResponseEntity<Map<String, Object>> validateFields(@RequestBody Map<String, String> fields) {
        return ResponseEntity.ok(profanityService.validateFields(fields));
    }

    /**
     * 텍스트 필터링 (마스킹)
     */
    @PostMapping("/filter")
    public ResponseEntity<Map<String, String>> filter(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String filtered = profanityService.filter(text);
        return ResponseEntity.ok(Map.of("original", text, "filtered", filtered));
    }
}