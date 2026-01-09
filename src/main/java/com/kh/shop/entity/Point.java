package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "point")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 포인트 타입: EARN(적립), USE(사용), EXPIRE(만료), CANCEL(취소)
    @Column(name = "point_type", length = 20, nullable = false)
    private String pointType;

    // 포인트 금액 (적립: 양수, 사용/만료: 음수)
    @Column(name = "point_amount", nullable = false)
    private Integer pointAmount;

    // 잔여 포인트 (적립 시점의 잔여 금액, 사용/만료 시 차감됨)
    @Column(name = "remaining_amount")
    private Integer remainingAmount;

    // 포인트 설명
    @Column(name = "description", length = 200)
    private String description;

    // 관련 주문 ID
    @Column(name = "order_id")
    private Long orderId;

    // 만료일 (적립 포인트의 유효기간)
    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    // 만료 처리 여부
    @Column(name = "is_expired")
    private Boolean isExpired;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.isExpired == null) this.isExpired = false;
        if (this.remainingAmount == null && "EARN".equals(this.pointType)) {
            this.remainingAmount = this.pointAmount;
        }
    }

    // 사용 가능한 포인트인지 확인
    public boolean isUsable() {
        if (!"EARN".equals(pointType)) return false;
        if (isExpired) return false;
        if (remainingAmount == null || remainingAmount <= 0) return false;
        if (expireDate != null && LocalDateTime.now().isAfter(expireDate)) return false;
        return true;
    }
}