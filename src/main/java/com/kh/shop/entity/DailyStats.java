package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"stat_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DailyStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    // 주문 통계
    @Column(name = "order_count")
    private Integer orderCount;

    @Column(name = "order_amount")
    private Long orderAmount;

    @Column(name = "cancelled_count")
    private Integer cancelledCount;

    @Column(name = "cancelled_amount")
    private Long cancelledAmount;

    // 회원 통계
    @Column(name = "new_user_count")
    private Integer newUserCount;

    @Column(name = "total_user_count")
    private Integer totalUserCount;

    // 상품 통계
    @Column(name = "new_product_count")
    private Integer newProductCount;

    @Column(name = "total_product_count")
    private Integer totalProductCount;

    // 리뷰 통계
    @Column(name = "new_review_count")
    private Integer newReviewCount;

    @Column(name = "total_review_count")
    private Integer totalReviewCount;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.orderCount == null) this.orderCount = 0;
        if (this.orderAmount == null) this.orderAmount = 0L;
        if (this.cancelledCount == null) this.cancelledCount = 0;
        if (this.cancelledAmount == null) this.cancelledAmount = 0L;
        if (this.newUserCount == null) this.newUserCount = 0;
        if (this.totalUserCount == null) this.totalUserCount = 0;
        if (this.newProductCount == null) this.newProductCount = 0;
        if (this.totalProductCount == null) this.totalProductCount = 0;
        if (this.newReviewCount == null) this.newReviewCount = 0;
        if (this.totalReviewCount == null) this.totalReviewCount = 0;
    }
}