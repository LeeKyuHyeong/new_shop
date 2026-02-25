package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "batch_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchConfig {

    @Id
    @Column(name = "batch_id", length = 50)
    private String batchId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @PrePersist
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
