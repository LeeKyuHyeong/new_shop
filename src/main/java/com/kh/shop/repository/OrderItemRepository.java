package com.kh.shop.repository;

import com.kh.shop.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderOrderIdAndUseYn(Long orderId, String useYn);

    @Query("SELECT COALESCE(p.category.categoryName, '미분류'), SUM(oi.quantity), SUM(oi.totalPrice) " +
            "FROM OrderItem oi " +
            "JOIN oi.product p " +
            "JOIN oi.order o " +
            "WHERE o.createdDate >= :startDate AND o.createdDate < :endDate " +
            "AND o.orderStatus != 'CANCELLED' " +
            "GROUP BY p.category.categoryName " +
            "ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> findCategoryStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(oi.color, '색상없음'), SUM(oi.quantity), SUM(oi.totalPrice) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.createdDate >= :startDate AND o.createdDate < :endDate " +
            "AND o.orderStatus != 'CANCELLED' " +
            "GROUP BY oi.color " +
            "ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> findColorStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COALESCE(oi.size, '사이즈없음'), SUM(oi.quantity), SUM(oi.totalPrice) " +
            "FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE o.createdDate >= :startDate AND o.createdDate < :endDate " +
            "AND o.orderStatus != 'CANCELLED' " +
            "GROUP BY oi.size " +
            "ORDER BY SUM(oi.totalPrice) DESC")
    List<Object[]> findSizeStatsByDateRange(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    // 특정 사용자가 특정 상품을 구매(배송완료)했는지 확인
    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi " +
            "JOIN oi.order o " +
            "WHERE oi.product.productId = :productId " +
            "AND o.user.userId = :userId " +
            "AND o.orderStatus = 'DELIVERED'")
    boolean existsPurchasedProduct(@Param("productId") Long productId, @Param("userId") String userId);
}