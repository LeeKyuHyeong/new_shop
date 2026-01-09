package com.kh.shop.service;

import com.kh.shop.entity.BatchLog;
import com.kh.shop.repository.BatchLogRepository;
import com.kh.shop.scheduler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchService {

    private final BatchLogRepository batchLogRepository;

    // 기존 스케줄러
    private final ProductBatchScheduler productBatchScheduler;
    private final OrderStatusBatchScheduler orderStatusBatchScheduler;
    private final OrderCreateBatchScheduler orderCreateBatchScheduler;
    private final UserSignupBatchScheduler userSignupBatchScheduler;
    private final CartCleanupBatchScheduler cartCleanupBatchScheduler;
    private final OrderCancelBatchScheduler orderCancelBatchScheduler;
    private final BestProductBatchScheduler bestProductBatchScheduler;
    private final DormantUserBatchScheduler dormantUserBatchScheduler;

    // 신규 스케줄러
    private final LowStockAlertBatchScheduler lowStockAlertBatchScheduler;
    private final ReviewRequestBatchScheduler reviewRequestBatchScheduler;
    private final TempFileCleanupBatchScheduler tempFileCleanupBatchScheduler;
    private final WishlistPriceAlertBatchScheduler wishlistPriceAlertBatchScheduler;
    private final RestockAlertBatchScheduler restockAlertBatchScheduler;
    private final StatsAggregateBatchScheduler statsAggregateBatchScheduler;
    private final LogArchiveBatchScheduler logArchiveBatchScheduler;
    private final SessionCleanupBatchScheduler sessionCleanupBatchScheduler;
    private final BackupDatabaseBatchScheduler backupDatabaseBatchScheduler;
    private final ExpiredCouponBatchScheduler expiredCouponBatchScheduler;
    private final PointExpiryBatchScheduler pointExpiryBatchScheduler;
    private final CouponExpiryAlertBatchScheduler couponExpiryAlertBatchScheduler;
    private final SearchKeywordAggregateBatchScheduler searchKeywordAggregateBatchScheduler;
    private final ProductViewStatsBatchScheduler productViewStatsBatchScheduler;
    private final ProductImageBatchScheduler productImageBatchScheduler;

    // 배치 정보 맵
    private static final Map<String, BatchInfo> BATCH_INFO_MAP = new LinkedHashMap<>();

    static {
        // 기존 구현 배치
        BATCH_INFO_MAP.put("PRODUCT_CREATE", new BatchInfo(
                "PRODUCT_CREATE", "랜덤 상품 등록", "매시 10분", "카테고리에 맞는 랜덤 상품 1개 등록"));
        BATCH_INFO_MAP.put("ORDER_STATUS_UPDATE", new BatchInfo(
                "ORDER_STATUS_UPDATE", "주문 상태 업데이트", "매시 5분", "랜덤 주문 상태 변경"));
        BATCH_INFO_MAP.put("ORDER_CREATE", new BatchInfo(
                "ORDER_CREATE", "랜덤 주문 생성", "매시 15분", "랜덤 회원이 랜덤 상품 주문"));
        BATCH_INFO_MAP.put("USER_SIGNUP", new BatchInfo(
                "USER_SIGNUP", "랜덤 회원 가입", "매시 20분", "랜덤 회원 1명 가입"));
        BATCH_INFO_MAP.put("CART_CLEANUP", new BatchInfo(
                "CART_CLEANUP", "장바구니 정리", "매일 03:00", "7일 이상 방치된 장바구니 삭제"));
        BATCH_INFO_MAP.put("ORDER_CANCEL", new BatchInfo(
                "ORDER_CANCEL", "미결제 주문 취소", "매시 30분", "24시간 이상 미결제 주문 자동 취소"));
        BATCH_INFO_MAP.put("BEST_PRODUCT_UPDATE", new BatchInfo(
                "BEST_PRODUCT_UPDATE", "베스트 상품 갱신", "매일 00:00", "주문량 기반 베스트 상품 순위 갱신"));
        BATCH_INFO_MAP.put("DORMANT_USER", new BatchInfo(
                "DORMANT_USER", "휴면 계정 처리", "매일 02:00", "1년 이상 미접속 계정 휴면 전환"));

        // 신규 구현 배치
        BATCH_INFO_MAP.put("LOW_STOCK_ALERT", new BatchInfo(
                "LOW_STOCK_ALERT", "재고 부족 알림", "매일 09:00", "재고 10개 이하 상품 관리자 알림"));
        BATCH_INFO_MAP.put("REVIEW_REQUEST", new BatchInfo(
                "REVIEW_REQUEST", "리뷰 작성 요청", "매일 10:00", "배송완료 7일 후 리뷰 작성 요청"));
        BATCH_INFO_MAP.put("EXPIRED_COUPON", new BatchInfo(
                "EXPIRED_COUPON", "쿠폰 만료 처리", "매일 00:30", "만료된 쿠폰 비활성화 처리"));
        BATCH_INFO_MAP.put("STATS_AGGREGATE", new BatchInfo(
                "STATS_AGGREGATE", "통계 데이터 집계", "매일 01:00", "일별/월별 매출, 방문자 통계 집계"));
        BATCH_INFO_MAP.put("TEMP_FILE_CLEANUP", new BatchInfo(
                "TEMP_FILE_CLEANUP", "임시 파일 정리", "매일 04:00", "7일 이상 된 임시 업로드 파일 삭제"));
        BATCH_INFO_MAP.put("WISHLIST_PRICE_ALERT", new BatchInfo(
                "WISHLIST_PRICE_ALERT", "위시리스트 가격 알림", "매일 08:00", "찜한 상품 할인 시작 시 알림"));
        BATCH_INFO_MAP.put("POINT_EXPIRY", new BatchInfo(
                "POINT_EXPIRY", "포인트 만료 처리", "매일 00:00", "유효기간 지난 적립금 소멸"));
        BATCH_INFO_MAP.put("SEARCH_KEYWORD_AGGREGATE", new BatchInfo(
                "SEARCH_KEYWORD_AGGREGATE", "인기 검색어 집계", "매시 00분", "실시간 인기 검색어 순위 갱신"));
        BATCH_INFO_MAP.put("RESTOCK_ALERT", new BatchInfo(
                "RESTOCK_ALERT", "재입고 알림", "매시 10분", "품절 상품 재입고 시 알림 발송"));
        BATCH_INFO_MAP.put("PRODUCT_VIEW_STATS", new BatchInfo(
                "PRODUCT_VIEW_STATS", "상품 조회수 집계", "매일 01:30", "일별 상품 조회수 통계 집계"));
        BATCH_INFO_MAP.put("COUPON_EXPIRY_ALERT", new BatchInfo(
                "COUPON_EXPIRY_ALERT", "쿠폰 만료 예정 알림", "매일 09:00", "3일 내 만료 예정 쿠폰 알림"));
        BATCH_INFO_MAP.put("SESSION_CLEANUP", new BatchInfo(
                "SESSION_CLEANUP", "세션 정리", "매시 00분", "만료된 세션 데이터 정리"));
        BATCH_INFO_MAP.put("LOG_ARCHIVE", new BatchInfo(
                "LOG_ARCHIVE", "로그 아카이브", "매주 일요일 03:00", "30일 이상 된 로그 아카이브 처리"));
        BATCH_INFO_MAP.put("BACKUP_DATABASE", new BatchInfo(
                "BACKUP_DATABASE", "데이터베이스 백업", "매일 05:00", "DB 자동 백업 및 오래된 백업 정리"));
        BATCH_INFO_MAP.put("PRODUCT_IMAGE_GENERATE", new BatchInfo(
                "PRODUCT_IMAGE_GENERATE", "상품 이미지 생성", "08:00, 14:00, 20:00", "이미지 없는 상품에 AI 이미지 자동 생성 (무료티어 일일40장)"));
    }

    /**
     * 모든 배치 정보 조회
     */
    public List<Map<String, Object>> getAllBatchInfo() {
        List<Map<String, Object>> result = new ArrayList<>();

        for (BatchInfo info : BATCH_INFO_MAP.values()) {
            Map<String, Object> batchData = new HashMap<>();
            batchData.put("batchId", info.batchId);
            batchData.put("batchName", info.batchName);
            batchData.put("schedule", info.schedule);
            batchData.put("description", info.description);

            // 마지막 로그 조회
            Optional<BatchLog> lastLog = batchLogRepository.findTopByBatchIdOrderByStartedAtDesc(info.batchId);
            if (lastLog.isPresent()) {
                BatchLog log = lastLog.get();
                batchData.put("lastStatus", log.getStatus());
                batchData.put("lastExecutedAt", log.getStartedAt());
                batchData.put("lastMessage", log.getMessage());
                batchData.put("lastExecutionTime", log.getExecutionTime());
            } else {
                batchData.put("lastStatus", "NEVER");
                batchData.put("lastExecutedAt", null);
                batchData.put("lastMessage", "실행 기록 없음");
                batchData.put("lastExecutionTime", null);
            }

            result.add(batchData);
        }

        return result;
    }

    /**
     * 배치 수동 실행
     */
    @Transactional
    public Map<String, Object> executeBatch(String batchId, String triggeredBy) {
        Map<String, Object> result = new HashMap<>();

        if (!BATCH_INFO_MAP.containsKey(batchId)) {
            result.put("success", false);
            result.put("message", "존재하지 않는 배치입니다.");
            return result;
        }

        BatchInfo info = BATCH_INFO_MAP.get(batchId);

        // 로그 시작
        BatchLog logEntry = BatchLog.builder()
                .batchId(batchId)
                .batchName(info.batchName)
                .status("RUNNING")
                .triggeredBy(triggeredBy != null ? triggeredBy : "MANUAL")
                .startedAt(LocalDateTime.now())
                .build();
        batchLogRepository.save(logEntry);

        long startTime = System.currentTimeMillis();

        try {
            // 배치 실행
            String message = runBatch(batchId);

            // 성공 로그
            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("SUCCESS");
            logEntry.setMessage(message);
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);

            result.put("success", true);
            result.put("message", message);

        } catch (Exception e) {
            // 실패 로그
            long executionTime = System.currentTimeMillis() - startTime;
            logEntry.setStatus("FAILED");
            logEntry.setMessage("오류: " + e.getMessage());
            logEntry.setFinishedAt(LocalDateTime.now());
            logEntry.setExecutionTime(executionTime);
            batchLogRepository.save(logEntry);

            result.put("success", false);
            result.put("message", "배치 실행 실패: " + e.getMessage());
            log.error("[배치] {} 수동 실행 실패", batchId, e);
        }

        return result;
    }

    /**
     * 실제 배치 실행
     */
    private String runBatch(String batchId) {
        switch (batchId) {
            // 기존 배치
            case "PRODUCT_CREATE":
                productBatchScheduler.createRandomProduct();
                return "랜덤 상품 1개가 등록되었습니다.";

            case "ORDER_STATUS_UPDATE":
                orderStatusBatchScheduler.updateOrderStatus();
                return "주문 상태가 업데이트되었습니다.";

            case "ORDER_CREATE":
                orderCreateBatchScheduler.createRandomOrder();
                return "랜덤 주문이 생성되었습니다.";

            case "USER_SIGNUP":
                userSignupBatchScheduler.createRandomUser();
                return "랜덤 회원이 가입되었습니다.";

            case "CART_CLEANUP":
                int deletedCount = cartCleanupBatchScheduler.cleanupAbandonedCartsManual();
                return deletedCount + "개의 방치된 장바구니가 정리되었습니다.";

            case "ORDER_CANCEL":
                int cancelledCount = orderCancelBatchScheduler.cancelUnpaidOrdersManual();
                return cancelledCount + "개의 미결제 주문이 취소되었습니다.";

            case "BEST_PRODUCT_UPDATE":
                int bestCount = bestProductBatchScheduler.updateBestProductsManual();
                return "베스트 상품 " + bestCount + "개가 갱신되었습니다.";

            case "DORMANT_USER":
                int dormantCount = dormantUserBatchScheduler.processDormantUsersManual();
                return dormantCount + "개의 계정이 휴면 전환되었습니다.";

            // 신규 배치
            case "LOW_STOCK_ALERT":
                return lowStockAlertBatchScheduler.checkLowStockProductsManual();

            case "REVIEW_REQUEST":
                return reviewRequestBatchScheduler.sendReviewRequestsManual();

            case "TEMP_FILE_CLEANUP":
                return tempFileCleanupBatchScheduler.cleanupTempFilesManual();

            case "WISHLIST_PRICE_ALERT":
                return wishlistPriceAlertBatchScheduler.sendWishlistPriceAlertsManual();

            case "RESTOCK_ALERT":
                return restockAlertBatchScheduler.sendRestockAlertsManual();

            case "STATS_AGGREGATE":
                return statsAggregateBatchScheduler.aggregateDailyStatsManual();

            case "LOG_ARCHIVE":
                return logArchiveBatchScheduler.archiveOldLogsManual();

            case "SESSION_CLEANUP":
                return sessionCleanupBatchScheduler.cleanupExpiredSessionsManual();

            case "BACKUP_DATABASE":
                return backupDatabaseBatchScheduler.backupDatabaseManual();

            case "EXPIRED_COUPON":
                return expiredCouponBatchScheduler.deactivateExpiredCouponsManual();

            case "POINT_EXPIRY":
                return pointExpiryBatchScheduler.expirePointsManual();

            case "COUPON_EXPIRY_ALERT":
                return couponExpiryAlertBatchScheduler.sendCouponExpiryAlertsManual();

            case "SEARCH_KEYWORD_AGGREGATE":
                return searchKeywordAggregateBatchScheduler.aggregatePopularKeywordsManual();

            case "PRODUCT_VIEW_STATS":
                return productViewStatsBatchScheduler.aggregateProductViewStatsManual();

            case "PRODUCT_IMAGE_GENERATE":
                return productImageBatchScheduler.generateAllMissingImagesManual();

            default:
                throw new IllegalArgumentException("알 수 없는 배치: " + batchId);
        }
    }

    /**
     * 배치 정보 내부 클래스
     */
    private static class BatchInfo {
        String batchId;
        String batchName;
        String schedule;
        String description;

        BatchInfo(String batchId, String batchName, String schedule, String description) {
            this.batchId = batchId;
            this.batchName = batchName;
            this.schedule = schedule;
            this.description = description;
        }
    }
}