package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_coupon", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "coupon_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    private Long userCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    // 사용자별 만료일 (쿠폰 발급 후 N일 등)
    @Column(name = "expire_date")
    private LocalDateTime expireDate;

    // 사용 여부
    @Column(name = "is_used")
    private Boolean isUsed;

    // 사용일
    @Column(name = "used_date")
    private LocalDateTime usedDate;

    // 사용한 주문 ID
    @Column(name = "used_order_id")
    private Long usedOrderId;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.isUsed == null) this.isUsed = false;
    }

    // 쿠폰 사용 가능 여부
    public boolean isUsable() {
        if (isUsed) return false;
        if (expireDate != null && LocalDateTime.now().isAfter(expireDate)) return false;
        if (coupon != null && !coupon.isUsable()) return false;
        return true;
    }
}