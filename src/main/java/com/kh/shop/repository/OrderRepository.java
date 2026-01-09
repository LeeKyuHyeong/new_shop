package com.kh.shop.repository;

import com.kh.shop.entity.Order;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUseYnOrderByCreatedDateDesc(String useYn);
    List<Order> findByUserAndUseYnOrderByCreatedDateDesc(User user, String useYn);
    Optional<Order> findByOrderNumberAndUseYn(String orderNumber, String useYn);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.orderId = :orderId AND o.useYn = 'Y'")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    List<Order> findByOrderStatusAndUseYnOrderByCreatedDateDesc(String orderStatus, String useYn);
    long countByUserAndUseYn(User user, String useYn);

    @Query("SELECT COUNT(o) FROM Order o WHERE DATE(o.createdDate) = CURRENT_DATE")
    long countTodayOrders();

    // 미결제 주문 조회 (생성 후 N시간 이상 경과한 PENDING 상태 주문)
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PENDING' AND o.paymentStatus = 'PENDING' AND o.createdDate < :cutoffDate AND o.useYn = 'Y'")
    List<Order> findUnpaidOrders(@Param("cutoffDate") LocalDateTime cutoffDate);

    // 최근 N일간 상품별 판매량 조회
    @Query("SELECT oi.product.productId, SUM(oi.quantity) as totalQty " +
            "FROM OrderItem oi JOIN oi.order o " +
            "WHERE o.orderStatus IN ('PAID', 'PREPARING', 'SHIPPING', 'DELIVERED') " +
            "AND o.createdDate >= :startDate AND o.useYn = 'Y' " +
            "GROUP BY oi.product.productId ORDER BY totalQty DESC")
    List<Object[]> findProductSalesCount(@Param("startDate") LocalDateTime startDate);

    // 배송완료 후 N일 경과한 주문 조회 (리뷰 요청용)
    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'DELIVERED' AND o.deliveredAt <= :cutoffDate AND o.useYn = 'Y'")
    List<Order> findDeliveredOrdersForReview(@Param("cutoffDate") LocalDateTime cutoffDate);
}