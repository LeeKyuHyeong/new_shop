package com.kh.shop.util;

import com.kh.shop.entity.ProfanityWord;
import com.kh.shop.repository.ProfanityWordRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProfanityFilter {

    @Autowired
    private ProfanityWordRepository profanityWordRepository;

    // 비속어 목록 (카테고리별)
    private final Set<String> profanitySet = Collections.synchronizedSet(new HashSet<>());

    // 정규식 패턴 목록
    private final List<Pattern> profanityPatterns = new ArrayList<>();

    // 숫자/특수문자 → 한글 변환 맵
    private static final Map<Character, Character> CHAR_REPLACE_MAP = new HashMap<>();

    static {
        // 숫자/특수문자 → 한글 변환
        CHAR_REPLACE_MAP.put('1', 'ㅣ');
        CHAR_REPLACE_MAP.put('2', '이');
        CHAR_REPLACE_MAP.put('3', '삼');
        CHAR_REPLACE_MAP.put('4', '사');
        CHAR_REPLACE_MAP.put('5', '오');
        CHAR_REPLACE_MAP.put('6', '육');
        CHAR_REPLACE_MAP.put('7', '칠');
        CHAR_REPLACE_MAP.put('8', '팔');
        CHAR_REPLACE_MAP.put('9', '구');
        CHAR_REPLACE_MAP.put('0', '공');
        CHAR_REPLACE_MAP.put('@', '아');
        CHAR_REPLACE_MAP.put('!', 'ㅣ');
        CHAR_REPLACE_MAP.put('$', 's');
        CHAR_REPLACE_MAP.put('&', '앤');
    }

    @PostConstruct
    public void init() {
        loadFromDatabase();
        initializePatterns();
        log.info("[비속어 필터] 초기화 완료 - 총 {}개 단어 로드", profanitySet.size());
    }

    /**
     * DB에서 비속어 로드
     */
    public void loadFromDatabase() {
        try {
            List<ProfanityWord> words = profanityWordRepository.findByIsActiveTrue();
            profanitySet.clear();
            for (ProfanityWord word : words) {
                profanitySet.add(word.getWord().toLowerCase());
            }
            log.info("[비속어 필터] DB에서 {}개 단어 로드", words.size());
        } catch (Exception e) {
            log.warn("[비속어 필터] DB 로드 실패, 기본 목록 사용: {}", e.getMessage());
            initializeDefaultList();
        }
    }

    /**
     * 기본 비속어 목록 (DB 접근 실패 시)
     */
    private void initializeDefaultList() {
        addProfanities(
                // 기본 욕설
                "시발", "씨발", "씨팔", "병신", "지랄", "개새끼", "좆", "존나", "미친",
                "닥쳐", "꺼져", "뒤져", "씹", "ㅅㅂ", "ㅆㅂ", "ㅂㅅ", "ㅄ", "ㅈㄹ",
                "fuck", "shit", "bitch", "bastard", "asshole"
        );
    }

    /**
     * 정규식 패턴 초기화
     */
    private void initializePatterns() {
        profanityPatterns.clear();
        // 공백/특수문자 삽입 패턴
        profanityPatterns.add(Pattern.compile("시[\\s\\W]*발", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("씨[\\s\\W]*발", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("씨[\\s\\W]*팔", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("병[\\s\\W]*신", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("지[\\s\\W]*랄", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("개[\\s\\W]*새[\\s\\W]*끼", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("개[\\s\\W]*색[\\s\\W]*끼", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("미[\\s\\W]*친", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("존[\\s\\W]*나", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("좆[\\s\\W]*나", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("느[\\s\\W]*금[\\s\\W]*마", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("f[\\s\\W]*u[\\s\\W]*c[\\s\\W]*k", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("s[\\s\\W]*h[\\s\\W]*i[\\s\\W]*t", Pattern.CASE_INSENSITIVE));
        profanityPatterns.add(Pattern.compile("b[\\s\\W]*i[\\s\\W]*t[\\s\\W]*c[\\s\\W]*h", Pattern.CASE_INSENSITIVE));
    }

    /**
     * 비속어 추가 헬퍼
     */
    private void addProfanities(String... words) {
        for (String word : words) {
            profanitySet.add(word.toLowerCase());
        }
    }

    // 초성 매칭에서 제외할 짧은 패턴 (오탐 방지)
    private static final Set<String> CHOSUNG_EXCLUDE_SET = Set.of(
            "ㄷㅈ",  // 디자인, 대전 등에서 오탐
            "ㄴㅁ",  // 나무, 내말 등에서 오탐
            "ㅊㄴ"   // 친구 등에서 오탐
    );

    /**
     * 비속어 포함 여부 확인
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String normalized = normalizeText(text);
        String lowerText = text.toLowerCase();

        // 1. 단순 매칭
        for (String profanity : profanitySet) {
            if (lowerText.contains(profanity) || normalized.contains(profanity)) {
                return true;
            }
        }

        // 2. 정규식 매칭
        for (Pattern pattern : profanityPatterns) {
            if (pattern.matcher(text).find() || pattern.matcher(normalized).find()) {
                return true;
            }
        }

        // 3. 초성 변환 후 매칭 (짧은 패턴은 제외하여 오탐 방지)
        String chosungText = extractChosung(text);
        for (String profanity : profanitySet) {
            // 2글자 이하 초성 패턴은 오탐이 많으므로 제외 목록 확인
            if (profanity.length() <= 2 && isChosungOnly(profanity) && CHOSUNG_EXCLUDE_SET.contains(profanity)) {
                continue;
            }
            if (chosungText.contains(profanity)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 문자열이 초성으로만 구성되어 있는지 확인
     */
    private boolean isChosungOnly(String text) {
        for (char c : text.toCharArray()) {
            if (c < 'ㄱ' || c > 'ㅎ') {
                return false;
            }
        }
        return true;
    }

    /**
     * 비속어 필터링 (마스킹 처리)
     */
    public String filterProfanity(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;
        String lowerText = text.toLowerCase();

        // 1. 단순 매칭 마스킹
        for (String profanity : profanitySet) {
            if (lowerText.contains(profanity)) {
                String mask = "*".repeat(profanity.length());
                result = result.replaceAll("(?i)" + Pattern.quote(profanity), mask);
                lowerText = result.toLowerCase();
            }
        }

        // 2. 정규식 매칭 마스킹
        for (Pattern pattern : profanityPatterns) {
            result = pattern.matcher(result).replaceAll(m -> "*".repeat(m.group().length()));
        }

        return result;
    }

    /**
     * 감지된 비속어 목록 반환
     */
    public List<String> detectProfanities(String text) {
        List<String> detected = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return detected;
        }

        String normalized = normalizeText(text);
        String lowerText = text.toLowerCase();

        // 1. 단순 매칭
        for (String profanity : profanitySet) {
            if (lowerText.contains(profanity) || normalized.contains(profanity)) {
                if (!detected.contains(profanity)) {
                    detected.add(profanity);
                }
            }
        }

        // 2. 정규식 매칭
        for (Pattern pattern : profanityPatterns) {
            var matcher = pattern.matcher(text);
            while (matcher.find()) {
                String found = matcher.group();
                if (!detected.contains(found.toLowerCase())) {
                    detected.add(found);
                }
            }
        }

        // 3. 초성 매칭 (제외 목록 적용)
        String chosungText = extractChosung(text);
        for (String profanity : profanitySet) {
            if (profanity.length() <= 2 && isChosungOnly(profanity) && CHOSUNG_EXCLUDE_SET.contains(profanity)) {
                continue;
            }
            if (isChosungOnly(profanity) && chosungText.contains(profanity)) {
                if (!detected.contains(profanity)) {
                    detected.add(profanity + "(초성)");
                }
            }
        }

        return detected;
    }

    /**
     * 상세 감지 결과 반환 (어디서 매칭되었는지 표시)
     */
    public List<Map<String, Object>> detectProfanitiesDetailed(String text) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return results;
        }

        String normalized = normalizeText(text);
        String lowerText = text.toLowerCase();

        // 1. 단순 매칭
        for (String profanity : profanitySet) {
            int idx = lowerText.indexOf(profanity);
            if (idx >= 0) {
                Map<String, Object> match = new HashMap<>();
                match.put("word", profanity);
                match.put("type", "단어매칭");
                match.put("position", idx);
                match.put("context", getContextString(text, idx, profanity.length()));
                results.add(match);
            }
        }

        // 2. 정규식 매칭
        for (Pattern pattern : profanityPatterns) {
            var matcher = pattern.matcher(text);
            while (matcher.find()) {
                Map<String, Object> match = new HashMap<>();
                match.put("word", matcher.group());
                match.put("type", "패턴매칭");
                match.put("position", matcher.start());
                match.put("context", getContextString(text, matcher.start(), matcher.group().length()));
                results.add(match);
            }
        }

        // 3. 초성 매칭 (제외 목록 적용)
        String chosungText = extractChosung(text);
        for (String profanity : profanitySet) {
            if (profanity.length() <= 2 && isChosungOnly(profanity) && CHOSUNG_EXCLUDE_SET.contains(profanity)) {
                continue;
            }
            if (isChosungOnly(profanity)) {
                int idx = chosungText.indexOf(profanity);
                if (idx >= 0) {
                    Map<String, Object> match = new HashMap<>();
                    match.put("word", profanity);
                    match.put("type", "초성매칭");
                    match.put("position", idx);
                    match.put("context", getChosungContext(text, idx, profanity.length()));
                    results.add(match);
                }
            }
        }

        return results;
    }

    /**
     * 매칭된 위치 주변 컨텍스트 문자열 반환
     */
    private String getContextString(String text, int position, int length) {
        int start = Math.max(0, position - 5);
        int end = Math.min(text.length(), position + length + 5);
        String before = text.substring(start, position);
        String matched = text.substring(position, position + length);
        String after = text.substring(position + length, end);
        return (start > 0 ? "..." : "") + before + "[" + matched + "]" + after + (end < text.length() ? "..." : "");
    }

    /**
     * 초성 매칭된 위치의 원본 문자열 컨텍스트 반환
     */
    private String getChosungContext(String text, int chosungPosition, int length) {
        // 초성 위치를 원본 한글 위치로 변환
        int koreanCharCount = 0;
        int originalStart = 0;
        int originalEnd = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if ((c >= '가' && c <= '힣') || (c >= 'ㄱ' && c <= 'ㅎ')) {
                if (koreanCharCount == chosungPosition) {
                    originalStart = i;
                }
                if (koreanCharCount == chosungPosition + length - 1) {
                    originalEnd = i + 1;
                    break;
                }
                koreanCharCount++;
            }
        }

        if (originalEnd == 0) originalEnd = text.length();

        int start = Math.max(0, originalStart - 3);
        int end = Math.min(text.length(), originalEnd + 3);

        String before = text.substring(start, originalStart);
        String matched = text.substring(originalStart, originalEnd);
        String after = text.substring(originalEnd, end);

        return (start > 0 ? "..." : "") + before + "[" + matched + "]" + after + (end < text.length() ? "..." : "");
    }

    /**
     * 텍스트 정규화 (특수문자, 숫자 → 한글 변환)
     */
    private String normalizeText(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (CHAR_REPLACE_MAP.containsKey(c)) {
                sb.append(CHAR_REPLACE_MAP.get(c));
            } else {
                sb.append(c);
            }
        }
        // 공백, 특수문자 제거
        return sb.toString().replaceAll("[\\s\\W]", "").toLowerCase();
    }

    /**
     * 초성 추출
     */
    private String extractChosung(String text) {
        StringBuilder sb = new StringBuilder();
        char[] chosungs = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
        for (char c : text.toCharArray()) {
            if (c >= '가' && c <= '힣') {
                int chosungIndex = (c - '가') / (21 * 28);
                sb.append(chosungs[chosungIndex]);
            } else if (c >= 'ㄱ' && c <= 'ㅎ') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 메모리에 비속어 추가 (런타임)
     */
    public void addProfanity(String word) {
        if (word != null && !word.isEmpty()) {
            profanitySet.add(word.toLowerCase());
        }
    }

    /**
     * 메모리에서 비속어 제거 (런타임)
     */
    public void removeProfanity(String word) {
        if (word != null && !word.isEmpty()) {
            profanitySet.remove(word.toLowerCase());
        }
    }

    /**
     * 캐시 갱신
     */
    public void refreshCache() {
        loadFromDatabase();
        log.info("[비속어 필터] 캐시 갱신 완료 - 총 {}개 단어", profanitySet.size());
    }

    /**
     * 전체 비속어 목록 반환
     */
    public Set<String> getAllProfanities() {
        return new HashSet<>(profanitySet);
    }

    /**
     * 비속어 개수 반환
     */
    public int getProfanityCount() {
        return profanitySet.size();
    }
}