package com.kh.shop.repository;

import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import com.kh.shop.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 사용자의 위시리스트 조회 (상품 정보 포함)
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p LEFT JOIN FETCH p.category WHERE w.user = :user AND p.useYn = 'Y' ORDER BY w.createdDate DESC")
    List<Wishlist> findByUserWithProduct(@Param("user") User user);

    // 특정 사용자의 특정 상품 위시리스트 조회
    Optional<Wishlist> findByUserAndProduct(User user, Product product);

    // 위시리스트 존재 여부 확인
    boolean existsByUserAndProduct(User user, Product product);

    // 사용자의 위시리스트 개수
    long countByUser(User user);

    // 사용자의 위시리스트 삭제
    void deleteByUserAndProduct(User user, Product product);

    // 특정 상품의 찜 개수
    long countByProduct(Product product);

    // 사용자 ID로 위시리스트 삭제 (회원 탈퇴 시)
    void deleteByUser(User user);
}