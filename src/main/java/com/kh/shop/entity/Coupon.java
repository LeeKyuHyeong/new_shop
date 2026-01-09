package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "coupon_code", unique = true, nullable = false, length = 50)
    private String couponCode;

    @Column(name = "coupon_name", nullable = false, length = 100)
    private String couponName;

    @Column(name = "description", length = 500)
    private String description;

    // 할인 타입: PERCENT(%), AMOUNT(원)
    @Column(name = "discount_type", length = 20)
    private String discountType;

    // 할인 값
    @Column(name = "discount_value")
    private Integer discountValue;

    // 최소 주문 금액
    @Column(name = "min_order_amount")
    private Integer minOrderAmount;

    // 최대 할인 금액
    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    // 유효 기간
    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    // 총 발행 수량
    @Column(name = "total_quantity")
    private Integer totalQuantity;

    // 사용된 수량
    @Column(name = "used_quantity")
    private Integer usedQuantity;

    // 활성화 여부
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.usedQuantity == null) this.usedQuantity = 0;
        if (this.discountType == null) this.discountType = "PERCENT";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // 쿠폰 만료 여부
    public boolean isExpired() {
        return endDate != null && LocalDateTime.now().isAfter(endDate);
    }

    // 쿠폰 사용 가능 여부
    public boolean isUsable() {
        if (!isActive) return false;
        if (isExpired()) return false;
        if (startDate != null && LocalDateTime.now().isBefore(startDate)) return false;
        if (totalQuantity != null && usedQuantity >= totalQuantity) return false;
        return true;
    }
}