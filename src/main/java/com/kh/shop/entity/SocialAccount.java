package com.kh.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "social_account")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "social_id")
    private Long socialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 소셜 제공자: KAKAO, NAVER, GOOGLE
    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    // 소셜 제공자에서 부여한 고유 ID
    @Column(name = "provider_id", nullable = false, length = 100)
    private String providerId;

    // 소셜 이메일 (선택적)
    @Column(name = "social_email", length = 100)
    private String socialEmail;

    // 소셜 닉네임
    @Column(name = "social_name", length = 100)
    private String socialName;

    // 프로필 이미지 URL
    @Column(name = "profile_image", length = 500)
    private String profileImage;

    // 액세스 토큰 (필요시 저장)
    @Column(name = "access_token", length = 500)
    private String accessToken;

    // 리프레시 토큰 (필요시 저장)
    @Column(name = "refresh_token", length = 500)
    private String refreshToken;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}