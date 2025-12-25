package com.kh.shop.repository;

import com.kh.shop.entity.Cart;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.useYn = 'Y' AND p.useYn = 'Y' ORDER BY c.createdDate DESC")
    List<Cart> findByUserWithProduct(@Param("user") User user);

    Optional<Cart> findByUserAndProductAndUseYn(User user, Product product, String useYn);
    long countByUserAndUseYn(User user, String useYn);
    void deleteByUser(User user);

    @Query("SELECT c FROM Cart c JOIN FETCH c.product p WHERE c.user = :user AND c.cartId IN :cartIds AND c.useYn = 'Y'")
    List<Cart> findByUserAndCartIdIn(@Param("user") User user, @Param("cartIds") List<Long> cartIds);
}