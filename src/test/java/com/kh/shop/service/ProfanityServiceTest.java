package com.kh.shop.service;

import com.kh.shop.repository.ProfanityWordRepository;
import com.kh.shop.util.ProfanityFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * ProfanityService 테스트 케이스
 */
@ExtendWith(MockitoExtension.class)
class ProfanityServiceTest {

    @Mock
    private ProfanityFilter profanityFilter;

    @Mock
    private ProfanityWordRepository profanityWordRepository;

    @InjectMocks
    private ProfanityService profanityService;

    @Nested
    @DisplayName("validate - 텍스트 검증")
    class ValidateTest {

        @Test
        @DisplayName("비속어가 없는 텍스트는 isValid=true")
        void validTextReturnsTrue() {
            when(profanityFilter.containsProfanity("안녕하세요")).thenReturn(false);

            Map<String, Object> result = profanityService.validate("안녕하세요");

            assertTrue((Boolean) result.get("isValid"));
            assertFalse((Boolean) result.get("hasProfanity"));
            assertEquals("사용 가능한 텍스트입니다.", result.get("message"));
        }

        @Test
        @DisplayName("비속어가 있는 텍스트는 isValid=false와 상세 정보 반환")
        void invalidTextReturnsFalseWithDetails() {
            String text = "시발 뭐야";
            when(profanityFilter.containsProfanity(text)).thenReturn(true);
            when(profanityFilter.detectProfanities(text)).thenReturn(List.of("시발"));
            when(profanityFilter.detectProfanitiesDetailed(text)).thenReturn(List.of(
                    Map.of("word", "시발", "type", "단어매칭", "context", "[시발] 뭐야")
            ));
            when(profanityFilter.filterProfanity(text)).thenReturn("** 뭐야");

            Map<String, Object> result = profanityService.validate(text);

            assertFalse((Boolean) result.get("isValid"));
            assertTrue((Boolean) result.get("hasProfanity"));
            assertNotNull(result.get("detectedWords"));
            assertNotNull(result.get("detectedDetails"));
            assertNotNull(result.get("filteredText"));
            assertEquals("부적절한 표현이 포함되어 있습니다.", result.get("message"));
        }
    }

    @Nested
    @DisplayName("validateFields - 여러 필드 검증")
    class ValidateFieldsTest {

        @Test
        @DisplayName("모든 필드가 정상이면 isValid=true")
        void allFieldsValidReturnsTrue() {
            when(profanityFilter.containsProfanity(anyString())).thenReturn(false);

            Map<String, Object> result = profanityService.validateFields(Map.of(
                    "title", "좋은 상품",
                    "description", "품질이 좋습니다"
            ));

            assertTrue((Boolean) result.get("isValid"));
            assertFalse((Boolean) result.get("hasAnyProfanity"));
        }

        @Test
        @DisplayName("일부 필드에 비속어가 있으면 해당 필드 정보 반환")
        void someFieldsInvalidReturnsDetails() {
            when(profanityFilter.containsProfanity("좋은 상품")).thenReturn(false);
            when(profanityFilter.containsProfanity("시발 별로")).thenReturn(true);
            when(profanityFilter.detectProfanities("시발 별로")).thenReturn(List.of("시발"));

            Map<String, Object> result = profanityService.validateFields(Map.of(
                    "title", "좋은 상품",
                    "description", "시발 별로"
            ));

            assertFalse((Boolean) result.get("isValid"));
            assertTrue((Boolean) result.get("hasAnyProfanity"));

            @SuppressWarnings("unchecked")
            Map<String, Object> fieldResults = (Map<String, Object>) result.get("fieldResults");
            assertTrue(fieldResults.containsKey("description"));
        }
    }

    @Nested
    @DisplayName("containsProfanity / filter / detect - 위임 메서드")
    class DelegateMethodsTest {

        @Test
        @DisplayName("containsProfanity는 ProfanityFilter에 위임")
        void containsProfanityDelegates() {
            when(profanityFilter.containsProfanity("테스트")).thenReturn(true);

            assertTrue(profanityService.containsProfanity("테스트"));
        }

        @Test
        @DisplayName("filter는 ProfanityFilter에 위임")
        void filterDelegates() {
            when(profanityFilter.filterProfanity("시발")).thenReturn("**");

            assertEquals("**", profanityService.filter("시발"));
        }

        @Test
        @DisplayName("detect는 ProfanityFilter에 위임")
        void detectDelegates() {
            when(profanityFilter.detectProfanities("시발")).thenReturn(List.of("시발"));

            List<String> result = profanityService.detect("시발");

            assertEquals(1, result.size());
            assertEquals("시발", result.get(0));
        }
    }

    @Nested
    @DisplayName("getCategories - 카테고리 목록")
    class GetCategoriesTest {

        @Test
        @DisplayName("정의된 카테고리 목록 반환")
        void returnsDefinedCategories() {
            List<String> categories = profanityService.getCategories();

            assertTrue(categories.contains("욕설"));
            assertTrue(categories.contains("비하"));
            assertTrue(categories.contains("성적"));
            assertTrue(categories.contains("혐오"));
            assertTrue(categories.contains("영어"));
            assertTrue(categories.contains("초성"));
            assertTrue(categories.contains("변형"));
            assertTrue(categories.contains("기타"));
        }
    }
}
