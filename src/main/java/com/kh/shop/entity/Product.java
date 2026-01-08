package com.kh.shop.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_price", nullable = false)
    private Integer productPrice;

    @Column(name = "product_discount")
    private Integer productDiscount;

    @Column(name = "product_stock")
    private Integer productStock;

    @Column(name = "product_description", columnDefinition = "TEXT")
    private String productDescription;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "product_order")
    private Integer productOrder;

    @Column(name = "sales_count")
    private Integer salesCount;

    @Column(name = "best_rank")
    private Integer bestRank;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.useYn = "Y";
        if (this.productOrder == null) {
            this.productOrder = 0;
        }
        if (this.productDiscount == null) {
            this.productDiscount = 0;
        }
        if (this.productStock == null) {
            this.productStock = 0;
        }
        if (this.salesCount == null) {
            this.salesCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // 할인 적용 가격
    public Integer getDiscountedPrice() {
        if (productDiscount == null || productDiscount == 0) {
            return productPrice;
        }
        return productPrice - (productPrice * productDiscount / 100);
    }

    // 카테고리 ID 반환
    public Integer getCategoryId() {
        return category != null ? category.getCategoryId() : null;
    }
}