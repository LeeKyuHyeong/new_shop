package com.kh.shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "template_type", nullable = false, length = 20)
    private String templateType;  // ITEM, STYLE, MATERIAL, DESCRIPTION

    @Column(name = "template_value", nullable = false, length = 500)
    private String templateValue;

    @Column(name = "use_yn", length = 1)
    @Builder.Default
    private String useYn = "Y";

    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.useYn == null) {
            this.useYn = "Y";
        }
    }
}