package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(name = "batch_id", nullable = false, length = 50)
    private String batchId;

    @Column(name = "batch_name", nullable = false, length = 100)
    private String batchName;

    @Column(name = "status", nullable = false, length = 20)
    private String status;  // RUNNING, SUCCESS, FAILED

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "execution_time")
    private Long executionTime;  // 밀리초

    @Column(name = "triggered_by", length = 20)
    private String triggeredBy;  // SCHEDULER, MANUAL

    @PrePersist
    public void prePersist() {
        if (this.startedAt == null) {
            this.startedAt = LocalDateTime.now();
        }
    }

    // 상태 한글 변환
    public String getStatusName() {
        if (status == null) return "";
        switch (status) {
            case "RUNNING": return "실행중";
            case "SUCCESS": return "성공";
            case "FAILED": return "실패";
            default: return status;
        }
    }

    // 실행 시간 포맷
    public String getExecutionTimeFormatted() {
        if (executionTime == null) return "-";
        if (executionTime < 1000) {
            return executionTime + "ms";
        } else {
            return String.format("%.2fs", executionTime / 1000.0);
        }
    }
}