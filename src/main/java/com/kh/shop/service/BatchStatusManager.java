package com.kh.shop.service;

import com.kh.shop.entity.BatchConfig;
import com.kh.shop.repository.BatchConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStatusManager {

    private final BatchConfigRepository batchConfigRepository;
    private final ConcurrentHashMap<String, Boolean> enabledCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        batchConfigRepository.findAll().forEach(config ->
                enabledCache.put(config.getBatchId(), config.isEnabled())
        );
        log.info("[배치설정] 배치 활성화 상태 캐시 로드 완료: {}건", enabledCache.size());
    }

    /**
     * 배치 활성화 여부 조회 (기본값: true)
     */
    public boolean isEnabled(String batchId) {
        return enabledCache.getOrDefault(batchId, true);
    }

    /**
     * 배치 활성화/비활성화 토글
     */
    public void setEnabled(String batchId, boolean enabled, String updatedBy) {
        BatchConfig config = batchConfigRepository.findById(batchId)
                .orElse(BatchConfig.builder().batchId(batchId).build());
        config.setEnabled(enabled);
        config.setUpdatedBy(updatedBy);
        batchConfigRepository.save(config);

        enabledCache.put(batchId, enabled);
        log.info("[배치설정] {} → {} (by {})", batchId, enabled ? "활성화" : "비활성화", updatedBy);
    }

    /**
     * 전체 배치 활성화 상태 조회
     */
    public Map<String, Boolean> getAllEnabledStates() {
        return Map.copyOf(enabledCache);
    }
}
