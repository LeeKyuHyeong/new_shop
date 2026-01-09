package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "search_keyword")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SearchKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "keyword_id")
    private Long keywordId;

    @Column(name = "keyword", nullable = false, length = 100)
    private String keyword;

    @Column(name = "search_count")
    private Integer searchCount;

    @Column(name = "search_date")
    private LocalDate searchDate;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.searchCount == null) this.searchCount = 1;
        if (this.searchDate == null) this.searchDate = LocalDate.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}