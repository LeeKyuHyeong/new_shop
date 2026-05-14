package com.kh.shop.util;

/**
 * JPA / SQL LIKE 검색용 키워드 이스케이프 유틸.
 *
 * 사용자가 입력한 키워드에 `%`, `_`, `\` 가 들어가면 LIKE 의 와일드카드로 해석되어
 *   - `%` 한 글자만 입력해도 전체 매치 -> 의도치 않은 광범위 결과
 *   - DoS 가능성 (`%%%%...` 류 패턴 매칭 비용)
 *   - 검색 결과 신뢰성 저하
 * 가 발생한다. 이를 막기 위해 와일드카드 문자를 백슬래시로 이스케이프한다.
 *
 * MariaDB / MySQL 의 LIKE 는 기본적으로 `\` 를 이스케이프 문자로 사용하므로 별도
 * `ESCAPE` 절을 명시하지 않아도 동작한다 (NO_BACKSLASH_ESCAPES 모드 비활성 가정).
 */
public final class LikeQueryUtil {

    private LikeQueryUtil() {
    }

    public static String escape(String keyword) {
        if (keyword == null) {
            return null;
        }
        // 백슬래시 먼저 (다른 이스케이프가 만든 백슬래시까지 중복 처리되는 것 방지)
        return keyword
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
