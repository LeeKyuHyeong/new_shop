package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_description", length = 500)
    private String categoryDescription;

    @Column(name = "category_order")
    @Builder.Default
    private Integer categoryOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    @Column(name = "use_yn", length = 1)
    @Builder.Default
    private String useYn = "Y";

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    // ==================== 배치용 컬럼 추가 ====================

    @Column(name = "min_price")
    @Builder.Default
    private Integer minPrice = 19000;

    @Column(name = "max_price")
    @Builder.Default
    private Integer maxPrice = 99000;

    @Column(name = "size_type", length = 20)
    @Builder.Default
    private String sizeType = "CLOTHES";  // CLOTHES, SHOES, FREE

    // ==========================================================

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.useYn == null) this.useYn = "Y";
        if (this.categoryOrder == null) this.categoryOrder = 0;
        if (this.minPrice == null) this.minPrice = 19000;
        if (this.maxPrice == null) this.maxPrice = 99000;
        if (this.sizeType == null) this.sizeType = "CLOTHES";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
    // 상위 카테고리 ID 반환 (JSP에서 사용)
    public Integer getParentId() {
        return this.parent != null ? this.parent.getCategoryId() : null;
    }
}