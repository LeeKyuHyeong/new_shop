package com.kh.shop.scheduler;

import com.kh.shop.repository.SearchKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchKeywordAggregateBatchScheduler {

    private final SearchKeywordRepository searchKeywordRepository;

    // 인기 검색어 집계 기간 (최근 7일)
    private static final int AGGREGATE_DAYS = 7;

    // 검색어 데이터 보관 기간 (30일)
    private static final int RETENTION_DAYS = 30;

    /**
     * 매시 00분에 실시간 인기 검색어 순위 갱신
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional(readOnly = true)
    public void aggregatePopularKeywords() {
        log.info("========== [배치] 인기 검색어 집계 시작 ==========");

        try {
            LocalDate startDate = LocalDate.now().minusDays(AGGREGATE_DAYS);

            // 인기 검색어 조회 (최근 7일간)
            List<Object[]> popularKeywords = searchKeywordRepository.findPopularKeywords(startDate);

            if (popularKeywords.isEmpty()) {
                log.info("[배치] 검색어 데이터가 없습니다.");
            } else {
                log.info("[배치] 인기 검색어 TOP 10:");
                int rank = 1;
                for (Object[] row : popularKeywords) {
                    if (rank > 10) break;
                    String keyword = (String) row[0];
                    Long count = (Long) row[1];
                    log.info("  {}위: {} ({}회)", rank++, keyword, count);
                }

                log.info("[배치] 총 {}개의 검색어 집계 완료", popularKeywords.size());
            }

        } catch (Exception e) {
            log.error("[배치] 인기 검색어 집계 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 인기 검색어 집계 종료 ==========");
    }

    /**
     * 매일 03:00에 오래된 검색어 데이터 정리
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOldKeywords() {
        log.info("[배치] 오래된 검색어 데이터 정리 시작");

        try {
            LocalDate cutoffDate = LocalDate.now().minusDays(RETENTION_DAYS);
            searchKeywordRepository.deleteBySearchDateBefore(cutoffDate);
            log.info("[배치] {}일 이전 검색어 데이터 정리 완료", RETENTION_DAYS);
        } catch (Exception e) {
            log.error("[배치] 검색어 데이터 정리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 수동 실행용 메서드
     */
    @Transactional(readOnly = true)
    public String aggregatePopularKeywordsManual() {
        log.info("[배치] 인기 검색어 집계 수동 실행");

        LocalDate startDate = LocalDate.now().minusDays(AGGREGATE_DAYS);
        List<Object[]> popularKeywords = searchKeywordRepository.findPopularKeywords(startDate);

        StringBuilder result = new StringBuilder();
        result.append("인기 검색어 TOP 5: ");

        int rank = 1;
        for (Object[] row : popularKeywords) {
            if (rank > 5) break;
            String keyword = (String) row[0];
            Long count = (Long) row[1];
            result.append(rank++).append(".").append(keyword).append("(").append(count).append(") ");
        }

        String resultStr = result.toString().trim();
        if (popularKeywords.isEmpty()) {
            resultStr = "검색어 데이터가 없습니다.";
        }

        log.info("[배치] {}", resultStr);
        return resultStr;
    }
}