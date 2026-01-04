package com.kh.shop.service;

import com.kh.shop.util.ProfanityFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfanityService {

    private final ProfanityFilter profanityFilter;

    /**
     * 비속어 포함 여부 확인
     */
    public boolean containsProfanity(String text) {
        return profanityFilter.containsProfanity(text);
    }

    /**
     * 비속어 필터링 (마스킹)
     */
    public String filter(String text) {
        return profanityFilter.filterProfanity(text);
    }

    /**
     * 감지된 비속어 목록 반환
     */
    public List<String> detect(String text) {
        return profanityFilter.detectProfanities(text);
    }

    /**
     * 텍스트 검증 결과 반환
     */
    public Map<String, Object> validate(String text) {
        Map<String, Object> result = new HashMap<>();
        boolean hasProfanity = profanityFilter.containsProfanity(text);

        result.put("isValid", !hasProfanity);
        result.put("hasProfanity", hasProfanity);

        if (hasProfanity) {
            List<String> detected = profanityFilter.detectProfanities(text);
            result.put("detectedWords", detected);
            result.put("filteredText", profanityFilter.filterProfanity(text));
            result.put("message", "부적절한 표현이 포함되어 있습니다.");
        } else {
            result.put("message", "사용 가능한 텍스트입니다.");
        }

        return result;
    }

    /**
     * 여러 필드 한번에 검증
     */
    public Map<String, Object> validateFields(Map<String, String> fields) {
        Map<String, Object> result = new HashMap<>();
        boolean hasAnyProfanity = false;
        Map<String, Object> fieldResults = new HashMap<>();

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();

            if (fieldValue != null && profanityFilter.containsProfanity(fieldValue)) {
                hasAnyProfanity = true;
                Map<String, Object> fieldResult = new HashMap<>();
                fieldResult.put("hasProfanity", true);
                fieldResult.put("detected", profanityFilter.detectProfanities(fieldValue));
                fieldResults.put(fieldName, fieldResult);
            }
        }

        result.put("isValid", !hasAnyProfanity);
        result.put("hasAnyProfanity", hasAnyProfanity);
        result.put("fieldResults", fieldResults);

        if (hasAnyProfanity) {
            result.put("message", "일부 필드에 부적절한 표현이 포함되어 있습니다.");
        }

        return result;
    }

    /**
     * 비속어 추가
     */
    public void addProfanity(String word) {
        profanityFilter.addProfanity(word);
        log.info("[비속어 필터] 단어 추가: {}", word);
    }

    /**
     * 비속어 제거
     */
    public void removeProfanity(String word) {
        profanityFilter.removeProfanity(word);
        log.info("[비속어 필터] 단어 제거: {}", word);
    }

    /**
     * 전체 비속어 목록
     */
    public Set<String> getAllProfanities() {
        return profanityFilter.getAllProfanities();
    }

    /**
     * 비속어 개수
     */
    public int getCount() {
        return profanityFilter.getProfanityCount();
    }
}