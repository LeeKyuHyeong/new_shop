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
    private final ProductBatchScheduler productBatchScheduler;
    private final OrderStatusBatchScheduler orderStatusBatchScheduler;
    private final OrderCreateBatchScheduler orderCreateBatchScheduler;
    private final UserSignupBatchScheduler userSignupBatchScheduler;
    private final CartCleanupBatchScheduler cartCleanupBatchScheduler;  // 추가

    // 배치 정보 맵
    private static final Map<String, BatchInfo> BATCH_INFO_MAP = new LinkedHashMap<>();

    static {
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
    public Map<String, Object> executeBatch(String batchId) {
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
                .triggeredBy("MANUAL")
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
                // TODO: 미결제 주문 취소 배치 구현 필요
                return "미결제 주문 취소가 완료되었습니다. (미구현)";

            case "BEST_PRODUCT_UPDATE":
                // TODO: 베스트 상품 갱신 배치 구현 필요
                return "베스트 상품 순위가 갱신되었습니다. (미구현)";

            case "DORMANT_USER":
                // TODO: 휴면 계정 처리 배치 구현 필요
                return "휴면 계정 처리가 완료되었습니다. (미구현)";

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