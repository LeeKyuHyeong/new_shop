package com.kh.shop.entity;

import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "popup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "popup_id")
    private Long popupId;

    @Column(name = "popup_title", nullable = false, length = 200)
    private String popupTitle;

    @Column(name = "popup_content", columnDefinition = "TEXT")
    private String popupContent;

    @Column(name = "popup_image_url", length = 500)
    private String popupImageUrl;

    @Column(name = "popup_link", length = 500)
    private String popupLink;

    @Column(name = "popup_width")
    private Integer popupWidth;

    @Column(name = "popup_height")
    private Integer popupHeight;

    @Column(name = "popup_top")
    private Integer popupTop;

    @Column(name = "popup_left")
    private Integer popupLeft;

    @Column(name = "popup_order")
    private Integer popupOrder;

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
        if (this.popupOrder == null) {
            this.popupOrder = 0;
        }
        if (this.popupWidth == null) {
            this.popupWidth = 400;
        }
        if (this.popupHeight == null) {
            this.popupHeight = 500;
        }
        if (this.popupTop == null) {
            this.popupTop = 100;
        }
        if (this.popupLeft == null) {
            this.popupLeft = 100;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}