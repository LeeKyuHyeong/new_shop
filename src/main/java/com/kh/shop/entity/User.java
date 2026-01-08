package com.kh.shop.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_password", nullable = false, length = 255)
    private String userPassword;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "birth")
    private LocalDate birth;

    @Column(name = "user_role", length = 20)
    private String userRole;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "user_status", length = 20)
    private String userStatus;  // ACTIVE, DORMANT, SUSPENDED

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.userRole = "USER";
        this.useYn = "Y";
        this.userStatus = "ACTIVE";
        this.lastLoginAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}