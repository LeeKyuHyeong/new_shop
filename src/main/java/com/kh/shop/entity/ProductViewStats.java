package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_view_stats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "view_date"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductViewStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_stats_id")
    private Long viewStatsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    @Column(name = "view_count")
    private Integer viewCount;

    @Column(name = "unique_view_count")
    private Integer uniqueViewCount;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.viewCount == null) this.viewCount = 0;
        if (this.uniqueViewCount == null) this.uniqueViewCount = 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}