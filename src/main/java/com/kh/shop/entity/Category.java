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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @Builder.Default
    @OrderBy("categoryOrder ASC")
    private List<Category> children = new ArrayList<>();

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(name = "category_description", length = 255)
    private String categoryDescription;

    @Column(name = "category_order")
    private Integer categoryOrder;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.useYn = "Y";
        if (this.categoryOrder == null) {
            this.categoryOrder = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // 상위 카테고리 여부 (최상위 카테고리인지)
    public boolean isRootCategory() {
        return this.parent == null;
    }

    // 상위 카테고리 ID 반환 (JSP에서 사용)
    public Integer getParentId() {
        return this.parent != null ? this.parent.getCategoryId() : null;
    }

}