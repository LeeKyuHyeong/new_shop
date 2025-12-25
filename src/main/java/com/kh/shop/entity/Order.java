package com.kh.shop.entity;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @Column(name = "receiver_address", nullable = false, length = 500)
    private String receiverAddress;

    @Column(name = "receiver_address_detail", length = 500)
    private String receiverAddressDetail;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "order_memo", length = 500)
    private String orderMemo;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "delivery_fee")
    private Integer deliveryFee;

    @Column(name = "final_price", nullable = false)
    private Integer finalPrice;

    @Column(name = "order_status", length = 20)
    private String orderStatus;  // PENDING, PAID, PREPARING, SHIPPING, DELIVERED, CANCELLED, REFUNDED

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;  // CARD, BANK, KAKAO, NAVER

    @Column(name = "payment_status", length = 20)
    private String paymentStatus;  // PENDING, COMPLETED, FAILED, CANCELLED

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.useYn = "Y";
        this.orderStatus = "PENDING";
        this.paymentStatus = "PENDING";
        if (this.deliveryFee == null) {
            this.deliveryFee = 0;
        }
        if (this.discountAmount == null) {
            this.discountAmount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    // 주문 상태 한글 변환
    public String getOrderStatusName() {
        if (orderStatus == null) return "";
        switch (orderStatus) {
            case "PENDING": return "주문대기";
            case "PAID": return "결제완료";
            case "PREPARING": return "상품준비중";
            case "SHIPPING": return "배송중";
            case "DELIVERED": return "배송완료";
            case "CANCELLED": return "주문취소";
            case "REFUNDED": return "환불완료";
            default: return orderStatus;
        }
    }

    // 결제 상태 한글 변환
    public String getPaymentStatusName() {
        if (paymentStatus == null) return "";
        switch (paymentStatus) {
            case "PENDING": return "결제대기";
            case "COMPLETED": return "결제완료";
            case "FAILED": return "결제실패";
            case "CANCELLED": return "결제취소";
            default: return paymentStatus;
        }
    }

    // 결제 수단 한글 변환
    public String getPaymentMethodName() {
        if (paymentMethod == null) return "";
        switch (paymentMethod) {
            case "CARD": return "신용카드";
            case "BANK": return "계좌이체";
            case "KAKAO": return "카카오페이";
            case "NAVER": return "네이버페이";
            default: return paymentMethod;
        }
    }

    // 주문 상품 총 개수
    public int getTotalQuantity() {
        return orderItems.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }
}