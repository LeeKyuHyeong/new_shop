package com.kh.shop.repository;

import com.kh.shop.entity.Order;
import com.kh.shop.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

    // N+1 방지: 주문 목록 조회 시 orderItems + product 까지 한 번에 fetch.
    // 동일 주문이 orderItems 수만큼 중복되지 않도록 DISTINCT 사용.
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product " +
            "WHERE o.user = :user AND o.useYn = :useYn " +
            "ORDER BY o.createdDate DESC")
    List<Order> findByUserAndUseYnWithItemsOrderByCreatedDateDesc(@Param("user") User user, @Param("useYn") String useYn);
    Optional<Order> findByOrderNumberAndUseYn(String orderNumber, String useYn);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems oi LEFT JOIN FETCH oi.product WHERE o.orderId = :orderId AND o.useYn = 'Y'")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    // 결제 검증/취소 동시성 차단용 락. PENDING -> PAID/CANCELLED 전환이 한 번만 일어나도록 보장.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId")
    Optional<Order> findByIdForUpdate(@Param("orderId") Long orderId);

    List<Order> findByOrderStatusAndUseYnOrderByCreatedDateDesc(String orderStatus, String useYn);

    // Admin: 페이징 (전체 / 상태별) — getAllOrders() 가 전체 메모리 로드하던 OOM 위험 제거용
    Page<Order> findByUseYnOrderByCreatedDateDesc(String useYn, Pageable pageable);
    Page<Order> findByOrderStatusAndUseYnOrderByCreatedDateDesc(String orderStatus, String useYn, Pageable pageable);
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