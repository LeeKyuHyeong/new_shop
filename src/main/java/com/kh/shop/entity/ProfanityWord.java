package com.kh.shop.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "profanity_word")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfanityWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Column(length = 50)
    private String category;  // 욕설, 비하, 성적, 혐오, 영어, 변형, 사용자추가

    @Column(length = 255)
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isSystem = false;  // 시스템 기본 비속어 여부

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private String createdBy;
}