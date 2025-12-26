package com.kh.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_setting")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 테마 설정: LIGHT, DARK
    @Column(name = "theme", length = 10)
    @Builder.Default
    private String theme = "LIGHT";

    // 언어 설정 (향후 확장용)
    @Column(name = "language", length = 10)
    @Builder.Default
    private String language = "KO";

    // 알림 수신 여부
    @Column(name = "notification_yn", length = 1)
    @Builder.Default
    private String notificationYn = "Y";

    // 이메일 수신 여부
    @Column(name = "email_receive_yn", length = 1)
    @Builder.Default
    private String emailReceiveYn = "Y";

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        if (this.theme == null) this.theme = "LIGHT";
        if (this.language == null) this.language = "KO";
        if (this.notificationYn == null) this.notificationYn = "Y";
        if (this.emailReceiveYn == null) this.emailReceiveYn = "Y";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}