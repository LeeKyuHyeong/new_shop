package com.kh.shop.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ProfanityFilter 테스트 케이스
 */
class ProfanityFilterTest {

    private ProfanityFilter profanityFilter;

    @BeforeEach
    void setUp() {
        profanityFilter = new ProfanityFilter();
        // DB 없이 기본 목록으로 초기화
        profanityFilter.addProfanity("시발");
        profanityFilter.addProfanity("씨발");
        profanityFilter.addProfanity("병신");
        profanityFilter.addProfanity("지랄");
        profanityFilter.addProfanity("미친");
        profanityFilter.addProfanity("존나");
        profanityFilter.addProfanity("ㅅㅂ");
        profanityFilter.addProfanity("ㅂㅅ");
        profanityFilter.addProfanity("ㅈㄹ");
        profanityFilter.addProfanity("ㄷㅈ");
        profanityFilter.addProfanity("ㄴㅁ");
        profanityFilter.addProfanity("ㅊㄴ");
        profanityFilter.addProfanity("fuck");
        profanityFilter.addProfanity("shit");
    }

    @Nested
    @DisplayName("containsProfanity - 비속어 포함 여부 확인")
    class ContainsProfanityTest {

        @Test
        @DisplayName("null 또는 빈 문자열은 false 반환")
        void nullOrEmptyReturnsFalse() {
            assertFalse(profanityFilter.containsProfanity(null));
            assertFalse(profanityFilter.containsProfanity(""));
        }

        @Test
        @DisplayName("비속어가 포함된 텍스트 감지")
        void detectsDirectProfanity() {
            assertTrue(profanityFilter.containsProfanity("시발 뭐야"));
            assertTrue(profanityFilter.containsProfanity("이 병신아"));
            assertTrue(profanityFilter.containsProfanity("지랄하네"));
        }

        @Test
        @DisplayName("대소문자 구분 없이 영어 비속어 감지")
        void detectsEnglishProfanityCaseInsensitive() {
            assertTrue(profanityFilter.containsProfanity("what the fuck"));
            assertTrue(profanityFilter.containsProfanity("FUCK YOU"));
            assertTrue(profanityFilter.containsProfanity("Shit happens"));
        }

        @Test
        @DisplayName("비속어가 없는 정상 텍스트")
        void normalTextPasses() {
            assertFalse(profanityFilter.containsProfanity("안녕하세요"));
            assertFalse(profanityFilter.containsProfanity("좋은 하루 되세요"));
            assertFalse(profanityFilter.containsProfanity("감사합니다"));
        }
    }

    @Nested
    @DisplayName("오탐 방지 테스트 - False Positive Prevention")
    class FalsePositivePreventionTest {

        @Test
        @DisplayName("'디자인'이 'ㄷㅈ' 초성으로 오탐되지 않음")
        void designDoesNotTriggerFalsePositive() {
            assertFalse(profanityFilter.containsProfanity("트렌디한 디자인"));
            assertFalse(profanityFilter.containsProfanity("디자인이 좋네요"));
            assertFalse(profanityFilter.containsProfanity("대전 여행"));
        }

        @Test
        @DisplayName("'나무', '내말'이 'ㄴㅁ' 초성으로 오탐되지 않음")
        void treeDoesNotTriggerFalsePositive() {
            assertFalse(profanityFilter.containsProfanity("나무가 예쁘다"));
            assertFalse(profanityFilter.containsProfanity("내말 들어봐"));
            assertFalse(profanityFilter.containsProfanity("너무 좋아"));
        }

        @Test
        @DisplayName("'친구'가 'ㅊㄴ' 초성으로 오탐되지 않음")
        void friendDoesNotTriggerFalsePositive() {
            assertFalse(profanityFilter.containsProfanity("친구와 만남"));
            assertFalse(profanityFilter.containsProfanity("친누나"));
        }

        @Test
        @DisplayName("상품 설명 문장이 오탐되지 않음")
        void productDescriptionPasses() {
            String description = "트렌디한 디자인의 포멀 벨벳 롱 트렌치코트입니다. 다양한 스타일링이 가능합니다.";
            assertFalse(profanityFilter.containsProfanity(description));
        }

        @Test
        @DisplayName("일반 문장들이 오탐되지 않음")
        void normalSentencesPass() {
            assertFalse(profanityFilter.containsProfanity("오늘 날씨가 좋습니다"));
            assertFalse(profanityFilter.containsProfanity("맛있는 음식을 먹었어요"));
            assertFalse(profanityFilter.containsProfanity("새로운 프로젝트를 시작합니다"));
            assertFalse(profanityFilter.containsProfanity("친구들과 함께 여행을 갔어요"));
        }
    }

    @Nested
    @DisplayName("filterProfanity - 비속어 마스킹")
    class FilterProfanityTest {

        @Test
        @DisplayName("비속어를 별표로 마스킹")
        void masksProfanityWithAsterisks() {
            assertEquals("** 뭐야", profanityFilter.filterProfanity("시발 뭐야"));
            assertEquals("이 **아", profanityFilter.filterProfanity("이 병신아"));
        }

        @Test
        @DisplayName("비속어가 없으면 원본 반환")
        void returnsOriginalIfNoProfanity() {
            String original = "안녕하세요 좋은 하루 되세요";
            assertEquals(original, profanityFilter.filterProfanity(original));
        }

        @Test
        @DisplayName("null 또는 빈 문자열 처리")
        void handlesNullAndEmpty() {
            assertNull(profanityFilter.filterProfanity(null));
            assertEquals("", profanityFilter.filterProfanity(""));
        }
    }

    @Nested
    @DisplayName("detectProfanities - 감지된 비속어 목록")
    class DetectProfanitiesTest {

        @Test
        @DisplayName("감지된 비속어 목록 반환")
        void returnsDetectedProfanities() {
            List<String> detected = profanityFilter.detectProfanities("시발 병신아");
            assertTrue(detected.contains("시발"));
            assertTrue(detected.contains("병신"));
        }

        @Test
        @DisplayName("비속어가 없으면 빈 목록 반환")
        void returnsEmptyListIfNoProfanity() {
            List<String> detected = profanityFilter.detectProfanities("안녕하세요");
            assertTrue(detected.isEmpty());
        }
    }

    @Nested
    @DisplayName("detectProfanitiesDetailed - 상세 감지 정보")
    class DetectProfanitiesDetailedTest {

        @Test
        @DisplayName("상세 감지 정보에 단어, 타입, 컨텍스트 포함")
        void returnsDetailedInfo() {
            List<Map<String, Object>> details = profanityFilter.detectProfanitiesDetailed("이건 시발 심하다");

            assertFalse(details.isEmpty());
            Map<String, Object> first = details.get(0);

            assertEquals("시발", first.get("word"));
            assertEquals("단어매칭", first.get("type"));
            assertNotNull(first.get("context"));
            assertTrue(first.get("context").toString().contains("[시발]"));
        }

        @Test
        @DisplayName("비속어가 없으면 빈 목록 반환")
        void returnsEmptyListIfNoProfanity() {
            List<Map<String, Object>> details = profanityFilter.detectProfanitiesDetailed("안녕하세요");
            assertTrue(details.isEmpty());
        }
    }

    @Nested
    @DisplayName("addProfanity / removeProfanity - 런타임 단어 관리")
    class RuntimeWordManagementTest {

        @Test
        @DisplayName("런타임에 비속어 추가")
        void addsProfanityAtRuntime() {
            assertFalse(profanityFilter.containsProfanity("테스트단어"));

            profanityFilter.addProfanity("테스트단어");

            assertTrue(profanityFilter.containsProfanity("테스트단어가 포함됨"));
        }

        @Test
        @DisplayName("런타임에 비속어 제거")
        void removesProfanityAtRuntime() {
            profanityFilter.addProfanity("임시단어");
            assertTrue(profanityFilter.containsProfanity("임시단어"));

            profanityFilter.removeProfanity("임시단어");

            assertFalse(profanityFilter.containsProfanity("임시단어"));
        }
    }

    @Nested
    @DisplayName("getAllProfanities / getProfanityCount")
    class ProfanityListManagementTest {

        @Test
        @DisplayName("전체 비속어 목록 반환")
        void returnsAllProfanities() {
            var all = profanityFilter.getAllProfanities();
            assertTrue(all.contains("시발"));
            assertTrue(all.contains("병신"));
        }

        @Test
        @DisplayName("비속어 개수 반환")
        void returnsProfanityCount() {
            int count = profanityFilter.getProfanityCount();
            assertTrue(count > 0);
        }
    }
}
