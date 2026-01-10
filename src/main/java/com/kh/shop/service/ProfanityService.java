package com.kh.shop.service;

import com.kh.shop.entity.ProfanityWord;
import com.kh.shop.repository.ProfanityWordRepository;
import com.kh.shop.util.ProfanityFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfanityService {

    private final ProfanityFilter profanityFilter;
    private final ProfanityWordRepository profanityWordRepository;

    // ==================== 검증 기능 ====================

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
            result.put("detectedDetails", profanityFilter.detectProfanitiesDetailed(text));
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

    // ==================== DB CRUD 기능 ====================

    /**
     * 비속어 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ProfanityWord> getList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return profanityWordRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    /**
     * 비속어 검색 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<ProfanityWord> search(String category, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return profanityWordRepository.findByFilter(
                (category != null && !category.isEmpty()) ? category : null,
                (keyword != null && !keyword.isEmpty()) ? keyword : null,
                pageable
        );
    }

    /**
     * 비속어 단건 조회
     */
    @Transactional(readOnly = true)
    public Optional<ProfanityWord> getById(Long id) {
        return profanityWordRepository.findById(id);
    }

    /**
     * 비속어 추가
     */
    @Transactional
    public ProfanityWord addWord(String word, String category, String description, String createdBy) {
        // 중복 체크
        if (profanityWordRepository.existsByWord(word.toLowerCase())) {
            throw new IllegalArgumentException("이미 등록된 단어입니다: " + word);
        }

        ProfanityWord profanityWord = ProfanityWord.builder()
                .word(word.toLowerCase())
                .category(category)
                .description(description)
                .isActive(true)
                .isSystem(false)
                .createdBy(createdBy)
                .build();

        ProfanityWord saved = profanityWordRepository.save(profanityWord);

        // 메모리 캐시에도 추가
        profanityFilter.addProfanity(word);

        log.info("[비속어 관리] 단어 추가: {} (카테고리: {}, 등록자: {})", word, category, createdBy);
        return saved;
    }

    /**
     * 비속어 수정
     */
    @Transactional
    public ProfanityWord updateWord(Long id, String word, String category, String description, Boolean isActive) {
        ProfanityWord profanityWord = profanityWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단어입니다."));

        String oldWord = profanityWord.getWord();

        // 단어 변경 시 중복 체크
        if (!oldWord.equals(word.toLowerCase()) && profanityWordRepository.existsByWord(word.toLowerCase())) {
            throw new IllegalArgumentException("이미 등록된 단어입니다: " + word);
        }

        profanityWord.setWord(word.toLowerCase());
        profanityWord.setCategory(category);
        profanityWord.setDescription(description);
        if (isActive != null) {
            profanityWord.setIsActive(isActive);
        }

        ProfanityWord saved = profanityWordRepository.save(profanityWord);

        // 메모리 캐시 갱신
        profanityFilter.removeProfanity(oldWord);
        if (Boolean.TRUE.equals(isActive)) {
            profanityFilter.addProfanity(word);
        }

        log.info("[비속어 관리] 단어 수정: {} -> {}", oldWord, word);
        return saved;
    }

    /**
     * 비속어 삭제
     */
    @Transactional
    public void deleteWord(Long id) {
        ProfanityWord profanityWord = profanityWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단어입니다."));

        // 시스템 기본 단어는 삭제 불가
        if (Boolean.TRUE.equals(profanityWord.getIsSystem())) {
            throw new IllegalArgumentException("시스템 기본 단어는 삭제할 수 없습니다.");
        }

        String word = profanityWord.getWord();
        profanityWordRepository.delete(profanityWord);

        // 메모리 캐시에서도 제거
        profanityFilter.removeProfanity(word);

        log.info("[비속어 관리] 단어 삭제: {}", word);
    }

    /**
     * 비속어 활성화/비활성화 토글
     */
    @Transactional
    public ProfanityWord toggleActive(Long id) {
        ProfanityWord profanityWord = profanityWordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 단어입니다."));

        boolean newStatus = !profanityWord.getIsActive();
        profanityWord.setIsActive(newStatus);

        ProfanityWord saved = profanityWordRepository.save(profanityWord);

        // 메모리 캐시 갱신
        if (newStatus) {
            profanityFilter.addProfanity(profanityWord.getWord());
        } else {
            profanityFilter.removeProfanity(profanityWord.getWord());
        }

        log.info("[비속어 관리] 단어 상태 변경: {} -> {}", profanityWord.getWord(), newStatus ? "활성화" : "비활성화");
        return saved;
    }

    /**
     * 일괄 삭제
     */
    @Transactional
    public int deleteMultiple(List<Long> ids) {
        int count = 0;
        for (Long id : ids) {
            try {
                deleteWord(id);
                count++;
            } catch (Exception e) {
                log.warn("[비속어 관리] 삭제 실패 (ID: {}): {}", id, e.getMessage());
            }
        }
        return count;
    }

    /**
     * 일괄 추가
     */
    @Transactional
    public int addMultiple(List<String> words, String category, String createdBy) {
        int count = 0;
        for (String word : words) {
            try {
                if (word != null && !word.trim().isEmpty()) {
                    addWord(word.trim(), category, null, createdBy);
                    count++;
                }
            } catch (Exception e) {
                log.warn("[비속어 관리] 추가 실패 (단어: {}): {}", word, e.getMessage());
            }
        }
        return count;
    }

    // ==================== 통계 기능 ====================

    /**
     * 통계 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalCount", profanityWordRepository.count());
        stats.put("activeCount", profanityWordRepository.countByIsActiveTrue());
        stats.put("systemCount", profanityWordRepository.countByIsSystemTrue());
        stats.put("userCount", profanityWordRepository.countByIsSystemFalse());
        stats.put("cacheCount", profanityFilter.getProfanityCount());

        // 카테고리별 개수
        List<Object[]> categoryStats = profanityWordRepository.countByCategory();
        Map<String, Long> categoryCount = new LinkedHashMap<>();
        for (Object[] row : categoryStats) {
            String category = (String) row[0];
            Long count = (Long) row[1];
            categoryCount.put(category != null ? category : "미분류", count);
        }
        stats.put("categoryCount", categoryCount);

        return stats;
    }

    /**
     * 캐시 갱신
     */
    public void refreshCache() {
        profanityFilter.refreshCache();
    }

    /**
     * 카테고리 목록
     */
    public List<String> getCategories() {
        return Arrays.asList("욕설", "비하", "성적", "혐오", "영어", "초성", "변형", "기타");
    }

    // ==================== 초기 데이터 설정 ====================

    /**
     * 기본 비속어 데이터 초기화
     */
    @Transactional
    public int initializeDefaultWords() {
        if (profanityWordRepository.count() > 0) {
            log.info("[비속어 관리] 이미 데이터가 존재합니다.");
            return 0;
        }

        List<ProfanityWord> defaultWords = createDefaultWords();
        profanityWordRepository.saveAll(defaultWords);

        // 캐시 갱신
        profanityFilter.refreshCache();

        log.info("[비속어 관리] 기본 데이터 {} 건 초기화 완료", defaultWords.size());
        return defaultWords.size();
    }

    private List<ProfanityWord> createDefaultWords() {
        List<ProfanityWord> words = new ArrayList<>();

        // 욕설
        addDefaultWord(words, "욕설", "시발", "씨발", "씨팔", "씨벌", "씨부랄", "씨부럴", "씨불", "시팔", "시벌");
        addDefaultWord(words, "욕설", "병신", "븅신", "빙신", "병딱", "병맛");
        addDefaultWord(words, "욕설", "지랄", "지럴", "지롤", "짓랄");
        addDefaultWord(words, "욕설", "염병", "엠병", "얌병", "옘병");
        addDefaultWord(words, "욕설", "좆", "좃", "조까", "조깐", "조낸", "존나", "존내", "졸라", "좆나");
        addDefaultWord(words, "욕설", "씹", "씹할", "씹새", "씹세", "씹년", "씹놈");
        addDefaultWord(words, "욕설", "개새끼", "개새기", "개세끼", "개세기", "개색끼", "개색기");
        addDefaultWord(words, "욕설", "개자식", "개차반", "개같은", "개년", "개놈", "개돼지");
        addDefaultWord(words, "욕설", "미친", "미친놈", "미친년", "미쳤");
        addDefaultWord(words, "욕설", "닥쳐", "닥치", "꺼져", "꺼저", "꺼지");
        addDefaultWord(words, "욕설", "뒤져", "뒤저", "뒤지", "뒈져", "뒈저", "뒈지");
        addDefaultWord(words, "욕설", "죽어", "죽을", "죽여", "뒤질");

        // 비하
        addDefaultWord(words, "비하", "찐따", "찐다", "진따", "등신", "덩신");
        addDefaultWord(words, "비하", "멍청", "멍청이", "바보", "빠보", "얼간이");
        addDefaultWord(words, "비하", "또라이", "돌아이", "또라리");
        addDefaultWord(words, "비하", "한남", "한녀", "김치녀", "김치남", "된장녀", "된장남");
        addDefaultWord(words, "비하", "느금마", "느그엄마", "느금", "니미", "니엄마", "니애미", "니애비");
        addDefaultWord(words, "비하", "애미", "애비", "에미", "에비");

        // 성적
        addDefaultWord(words, "성적", "보지", "자지", "섹스", "쎅스");
        addDefaultWord(words, "성적", "강간", "창녀", "창년", "매춘", "걸레");

        // 혐오
        addDefaultWord(words, "혐오", "애자", "앰생", "앰창", "정병");
        addDefaultWord(words, "혐오", "쪽바리", "짱깨", "짱개");

        // 초성 (2글자 초성은 오탐이 많아 3글자 이상만 포함)
        addDefaultWord(words, "초성", "ㅅㅂ", "ㅆㅂ", "ㅂㅅ", "ㅄ");
        addDefaultWord(words, "초성", "ㅈㄹ", "ㅈㄴ", "ㄱㅅㄲ", "ㄱㅅㄱ");
        addDefaultWord(words, "초성", "ㅁㅊ", "ㅁㅊㄴ", "ㄷㅊ", "ㄲㅈ");
        // "ㄷㅈ", "ㄴㅁ", "ㅊㄴ" 제거 - 디자인, 나무, 친구 등에서 오탐
        addDefaultWord(words, "초성", "ㄴㄱㅁ");

        // 영어
        addDefaultWord(words, "영어", "fuck", "fucking", "fucked", "fucker", "motherfucker");
        addDefaultWord(words, "영어", "shit", "bullshit", "damn", "dumbass", "jackass");
        addDefaultWord(words, "영어", "bitch", "bastard", "asshole", "ass");
        addDefaultWord(words, "영어", "dick", "cock", "pussy", "cunt");
        addDefaultWord(words, "영어", "whore", "slut", "faggot", "fag");
        addDefaultWord(words, "영어", "nigger", "nigga", "retard", "retarded");
        addDefaultWord(words, "영어", "idiot", "moron", "wtf", "stfu", "gtfo");

        // 변형
        addDefaultWord(words, "변형", "시1발", "씨1발", "시8", "씨8", "c발");
        addDefaultWord(words, "변형", "sibal", "ssibal", "tlqkf", "qudtls");
        addDefaultWord(words, "변형", "병1신", "byungsin", "jiral", "wlfkf");

        // 기타
        addDefaultWord(words, "기타", "엿먹", "빠구리", "후레자식", "쓰레기", "쓰렉");
        addDefaultWord(words, "기타", "허접", "호구", "호갱", "ㅗ", "ㅗㅗ");

        return words;
    }

    private void addDefaultWord(List<ProfanityWord> list, String category, String... words) {
        for (String word : words) {
            list.add(ProfanityWord.builder()
                    .word(word.toLowerCase())
                    .category(category)
                    .isActive(true)
                    .isSystem(true)
                    .createdBy("SYSTEM")
                    .build());
        }
    }
}