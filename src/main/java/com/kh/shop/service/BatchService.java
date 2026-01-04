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

    // 배치 정보 정의
    private static final Map<String, BatchInfo> BATCH_INFO_MAP = new LinkedHashMap<>();

    static {
        BATCH_INFO_MAP.put("PRODUCT_CREATE", new BatchInfo("PRODUCT_CREATE", "상품 자동 등록", "매시 10분", "랜덤 상품 1개를 자동 등록합니다."));
        BATCH_INFO_MAP.put("ORDER_STATUS_UPDATE", new BatchInfo("ORDER_STATUS_UPDATE", "주문 상태 변경", "매시 20분", "주문 상태를 다음 단계로 자동 변경합니다."));
        BATCH_INFO_MAP.put("ORDER_CREATE", new BatchInfo("ORDER_CREATE", "주문 자동 생성", "매시 15분", "랜덤 사용자가 랜덤 상품을 주문합니다."));
        BATCH_INFO_MAP.put("USER_SIGNUP", new BatchInfo("USER_SIGNUP", "회원 자동 가입", "매시 5분", "랜덤 회원 1명을 자동 생성합니다."));
        BATCH_INFO_MAP.put("CART_CLEANUP", new BatchInfo("CART_CLEANUP", "장바구니 정리", "매일 03:00", "7일 이상 방치된 장바구니를 정리합니다."));
        BATCH_INFO_MAP.put("ORDER_CANCEL", new BatchInfo("ORDER_CANCEL", "미결제 주문 취소", "매시 30분", "24시간 이상 미결제 주문을 자동 취소합니다."));
        BATCH_INFO_MAP.put("BEST_PRODUCT_UPDATE", new BatchInfo("BEST_PRODUCT_UPDATE", "베스트 상품 갱신", "매일 00:00", "주문량 기반으로 베스트 상품 순위를 갱신합니다."));
        BATCH_INFO_MAP.put("DORMANT_USER", new BatchInfo("DORMANT_USER", "휴면 계정 처리", "매일 02:00", "1년 이상 미접속 계정을 휴면 처리합니다."));
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

            // 최근 로그 조회
            Optional<BatchLog> latestLog = batchLogRepository.findTopByBatchIdOrderByStartedAtDesc(info.batchId);
            if (latestLog.isPresent()) {
                BatchLog logEntry = latestLog.get();
                batchData.put("lastStatus", logEntry.getStatus());
                batchData.put("lastStatusName", logEntry.getStatusName());
                batchData.put("lastMessage", logEntry.getMessage());
                batchData.put("lastExecutedAt", logEntry.getStartedAt());
                batchData.put("lastExecutionTime", logEntry.getExecutionTimeFormatted());
            } else {
                batchData.put("lastStatus", null);
                batchData.put("lastStatusName", "미실행");
                batchData.put("lastMessage", "아직 실행된 적이 없습니다.");
                batchData.put("lastExecutedAt", null);
                batchData.put("lastExecutionTime", "-");
            }

            // 현재 실행 중 여부
            boolean isRunning = batchLogRepository.existsByBatchIdAndStatus(info.batchId, "RUNNING");
            batchData.put("isRunning", isRunning);

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

        // 이미 실행 중인지 확인
        if (batchLogRepository.existsByBatchIdAndStatus(batchId, "RUNNING")) {
            result.put("success", false);
            result.put("message", "해당 배치가 이미 실행 중입니다.");
            return result;
        }

        BatchInfo info = BATCH_INFO_MAP.get(batchId);
        if (info == null) {
            result.put("success", false);
            result.put("message", "존재하지 않는 배치입니다.");
            return result;
        }

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
                // TODO: 장바구니 정리 배치 구현 필요
                return "장바구니 정리가 완료되었습니다. (미구현)";

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
     * 스케줄러에서 호출 - 로그 기록용
     */
    @Transactional
    public void logBatchExecution(String batchId, String status, String message, long executionTime) {
        BatchInfo info = BATCH_INFO_MAP.get(batchId);
        if (info == null) return;

        BatchLog logEntry = BatchLog.builder()
                .batchId(batchId)
                .batchName(info.batchName)
                .status(status)
                .message(message)
                .triggeredBy("SCHEDULER")
                .startedAt(LocalDateTime.now().minusNanos(executionTime * 1000000L))
                .finishedAt(LocalDateTime.now())
                .executionTime(executionTime)
                .build();
        batchLogRepository.save(logEntry);
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