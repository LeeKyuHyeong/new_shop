package com.kh.shop.repository;

import com.kh.shop.entity.Order;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}