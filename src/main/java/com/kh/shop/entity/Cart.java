package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Long cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "color", length = 50)
    private String color;

    @Column(name = "size", length = 50)
    private String size;

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
        if (this.quantity == null) this.quantity = 1;
    }

    @PreUpdate
    public void preUpdate() { this.updatedDate = LocalDateTime.now(); }

    public Integer getTotalPrice() {
        if (product == null) return 0;
        return product.getDiscountedPrice() * quantity;
    }

    // 옵션 표시용
    public String getOptionText() {
        StringBuilder sb = new StringBuilder();
        if (color != null && !color.isEmpty()) {
            sb.append(color);
        }
        if (size != null && !size.isEmpty()) {
            if (sb.length() > 0) sb.append(" / ");
            sb.append(size);
        }
        return sb.toString();
    }
}