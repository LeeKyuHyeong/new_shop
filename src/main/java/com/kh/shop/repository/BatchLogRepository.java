package com.kh.shop.repository;

import com.kh.shop.entity.BatchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BatchLogRepository extends JpaRepository<BatchLog, Long> {

    // 특정 배치의 최근 로그 조회
    Optional<BatchLog> findTopByBatchIdOrderByStartedAtDesc(String batchId);

    // 특정 배치의 로그 목록 조회
    List<BatchLog> findByBatchIdOrderByStartedAtDesc(String batchId);

    // 모든 배치의 최근 로그 조회
    @Query("SELECT b FROM BatchLog b WHERE b.startedAt = (SELECT MAX(b2.startedAt) FROM BatchLog b2 WHERE b2.batchId = b.batchId)")
    List<BatchLog> findLatestLogsByBatch();

    // 현재 실행 중인 배치 조회
    List<BatchLog> findByStatus(String status);

    // 특정 배치가 현재 실행 중인지 확인
    boolean existsByBatchIdAndStatus(String batchId, String status);

    // 오래된 로그 삭제 (아카이브)
    @Modifying
    @Query("DELETE FROM BatchLog b WHERE b.startedAt < :cutoffDate")
    int deleteByStartedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 오래된 로그 개수 조회
    @Query("SELECT COUNT(b) FROM BatchLog b WHERE b.startedAt < :cutoffDate")
    int countByStartedAtBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}