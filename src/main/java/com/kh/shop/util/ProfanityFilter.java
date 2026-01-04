package com.kh.shop.util;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

@Component
public class ProfanityFilter {

    // 비속어 목록 (카테고리별)
    private final Set<String> profanitySet = new HashSet<>();

    // 정규식 패턴 목록
    private final List<Pattern> profanityPatterns = new ArrayList<>();

    // 초성 매핑
    private static final Map<Character, Character> CHOSUNG_MAP = new HashMap<>();

    // 숫자/특수문자 → 한글 변환 맵
    private static final Map<Character, Character> CHAR_REPLACE_MAP = new HashMap<>();

    static {
        // 초성 매핑 (가~힣 → ㄱ~ㅎ)
        String[] chosung = {"ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ", "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"};
        for (int i = 0; i < chosung.length; i++) {
            CHOSUNG_MAP.put(chosung[i].charAt(0), chosung[i].charAt(0));
        }

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

    public ProfanityFilter() {
        initializeProfanityList();
        initializePatterns();
    }

    /**
     * 비속어 목록 초기화
     */
    private void initializeProfanityList() {
        // ========== 욕설/비속어 ==========
        addProfanities(
                // 기본 욕설
                "시발", "씨발", "씨팔", "씨벌", "씨부랄", "씨부럴", "씨불", "시팔", "시벌", "시부랄",
                "시부럴", "시불", "씹", "씹할", "씹새", "씹세", "씹년", "씹놈",
                "개새끼", "개새기", "개세끼", "개세기", "개색끼", "개색기", "개섀끼",
                "개자식", "개차반", "개같은", "개년", "개놈", "개돼지",
                "병신", "븅신", "빙신", "병딱", "병맛", "병먹",
                "지랄", "지럴", "지롤", "짓랄",
                "염병", "엠병", "얌병", "옘병",
                "좆", "좃", "조까", "조깐", "조낸", "존나", "존내", "졸라", "존니", "좆나",
                "미친", "미친놈", "미친년", "미치놈", "미치년", "미쳤",
                "닥쳐", "닥치", "닥쵸", "닥초",
                "꺼져", "꺼저", "꺼지",
                "뒤져", "뒤저", "뒤지", "뒈져", "뒈저", "뒈지", "디져", "디저", "디지",
                "죽어", "죽을", "죽여", "뒤질",

                // 비하/멸시 표현
                "찐따", "찐다", "진따", "진다", "찐찌버거",
                "등신", "덩신", "던신",
                "멍청", "멍청이", "멍청한",
                "바보", "빠보", "바봉",
                "얼간이", "얼간",
                "멍텅", "멍퉁",
                "또라이", "돌아이", "또라리", "돌아리",
                "한남", "한녀", "김치녀", "김치남", "된장녀", "된장남",
                "느금마", "느그엄마", "느금", "니미", "니엄마", "니애미", "니애비", "느개비",
                "애미", "애비", "에미", "에비",
                "노무", "노알라", "노무현",

                // 성적 비하
                "보지", "보짓", "봊이", "자지", "잦이", "잦지",
                "섹스", "쎅스", "섹쓰", "쎅쓰",
                "성교", "성관계", "성행위",
                "강간", "강갂",
                "창녀", "창년", "창놈",
                "매춘", "매춘부",
                "걸레", "걸래",
                "화냥", "화냥년",

                // 혐오 표현
                "장애인", "애자", "앰생", "앰창",
                "정신병", "정신병자", "정병",
                "게이", "호모", "레즈",
                "쪽바리", "짱깨", "짱개", "청국", "청궈", "흑형",

                // 신체/외모 비하
                "뚱땡이", "뚱뚱이", "돼지년", "돼지놈",
                "대머리", "빡빡이",

                // 기타 비속어
                "엿먹", "엿머거", "엿이나",
                "빠구리", "빠굴",
                "후레자식", "후래자식", "후레아들",
                "나쁜년", "나쁜놈",
                "쓰레기", "쓰래기", "쓰렉",
                "허접", "호구", "호갱",
                "ㅗ", "ㅗㅗ"
        );

        // ========== 초성 욕설 ==========
        addProfanities(
                "ㅅㅂ", "ㅆㅂ", "ㅂㅅ", "ㅄ",
                "ㅈㄹ", "ㅈㄴ", "ㅈㅣㄹ",
                "ㄱㅅㄲ", "ㄱㅅㄱ", "ㄱㅅ",
                "ㅁㅊ", "ㅁㅊㄴ", "ㅁㅊㄴㄴ",
                "ㄷㅊ", "ㄲㅈ", "ㄷㅈ",
                "ㄴㄱㅁ", "ㄴㅁ",
                "ㅆㅍ", "ㅊㄴ", "ㅊㄴㄴ",
                "ㅍㅌㅊ", "ㅂㄹ"
        );

        // ========== 영어 욕설 ==========
        addProfanities(
                "fuck", "shit", "damn", "bitch", "bastard", "asshole", "ass",
                "dick", "cock", "pussy", "cunt", "whore", "slut",
                "motherfucker", "fucker", "fucking", "fucked",
                "bullshit", "dumbass", "jackass",
                "wtf", "stfu", "gtfo", "lmfao",
                "retard", "retarded", "idiot", "moron",
                "faggot", "fag", "nigger", "nigga"
        );

        // ========== 변형 표현 ==========
        addProfanities(
                // 시발 변형
                "시1발", "씨1발", "시8", "씨8", "시발점", "c발", "ㅅ1ㅂ",
                "시bal", "씨bal", "sibal", "ssibal", "tlqkf",

                // 병신 변형
                "ㅂ1ㅅ1ㄴ", "병1신", "byungsin", "qudtls",

                // 지랄 변형
                "ㅈ1ㄹ", "지1랄", "jiral", "wlfkf",

                // 개새끼 변형
                "ㄱㅅㄲ", "개1새끼", "개색", "gaesaekki", "rotoRl",

                // 좆 변형
                "조1까", "좆1나", "zo까", "wh까"
        );
    }

    /**
     * 정규식 패턴 초기화
     */
    private void initializePatterns() {
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

        // 3. 초성 변환 후 매칭
        String chosungText = extractChosung(text);
        for (String profanity : profanitySet) {
            if (chosungText.contains(profanity)) {
                return true;
            }
        }

        return false;
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
                detected.add(profanity);
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

        return detected;
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
        for (char c : text.toCharArray()) {
            if (c >= '가' && c <= '힣') {
                int chosungIndex = (c - '가') / (21 * 28);
                char[] chosungs = {'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'};
                sb.append(chosungs[chosungIndex]);
            } else if (CHOSUNG_MAP.containsKey(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 비속어 추가 (런타임)
     */
    public void addProfanity(String word) {
        if (word != null && !word.isEmpty()) {
            profanitySet.add(word.toLowerCase());
        }
    }

    /**
     * 비속어 제거 (런타임)
     */
    public void removeProfanity(String word) {
        if (word != null && !word.isEmpty()) {
            profanitySet.remove(word.toLowerCase());
        }
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