package com.kh.shop.repository;

import com.kh.shop.entity.Cart;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.useYn = 'Y' AND p.useYn = 'Y' ORDER BY c.createdDate DESC")
    List<Cart> findByUserWithProduct(@Param("user") User user);

    // 기존 메소드 (호환성 유지)
    Optional<Cart> findByUserAndProductAndUseYn(User user, Product product, String useYn);

    // 옵션 포함 검색 (같은 상품이라도 옵션이 다르면 별도 항목)
    @Query("SELECT c FROM Cart c WHERE c.user = :user AND c.product = :product AND c.useYn = :useYn " +
            "AND ((:color IS NULL AND c.color IS NULL) OR c.color = :color) " +
            "AND ((:size IS NULL AND c.size IS NULL) OR c.size = :size)")
    Optional<Cart> findByUserAndProductAndOptionAndUseYn(
            @Param("user") User user,
            @Param("product") Product product,
            @Param("color") String color,
            @Param("size") String size,
            @Param("useYn") String useYn);

    long countByUserAndUseYn(User user, String useYn);
    void deleteByUser(User user);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.cartId IN :cartIds AND c.useYn = 'Y'")
    List<Cart> findByUserAndCartIdIn(@Param("user") User user, @Param("cartIds") List<Long> cartIds);

    @Query("SELECT c FROM Cart c WHERE c.updatedDate < :cutoffDate AND c.useYn = :useYn")
    List<Cart> findOldCarts(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("useYn") String useYn);

    // 7일 이상 방치된 장바구니 조회
    List<Cart> findByUpdatedDateBefore(LocalDateTime date);

    // 또는 createdDate 기준으로 하려면
    List<Cart> findByCreatedDateBefore(LocalDateTime date);

    // 삭제용 (벌크 삭제가 더 효율적)
    @Modifying
    @Query("DELETE FROM Cart c WHERE c.updatedDate < :date")
    int deleteByUpdatedDateBefore(@Param("date") LocalDateTime date);
}