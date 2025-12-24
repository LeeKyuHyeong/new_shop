package com.kh.shop.repository;

import com.kh.shop.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 사용중인 상품 전체 조회
    List<Product> findByUseYnOrderByProductOrderAsc(String useYn);

    // 카테고리별 상품 조회
    List<Product> findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(Integer categoryId, String useYn);

    // 상품 + 카테고리 fetch join
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder ASC")
    List<Product> findAllWithCategory(@Param("useYn") String useYn);

    // 상품 + 이미지 fetch join
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") Long productId);

    // 상품명 중복 체크
    Optional<Product> findByProductNameAndUseYn(String productName, String useYn);

    // 신상품 조회 (최근 등록순)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.createdDate DESC")
    List<Product> findNewProducts(@Param("useYn") String useYn);

    // 베스트 상품 조회 (productOrder 높은순)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder DESC")
    List<Product> findBestProducts(@Param("useYn") String useYn);

    // 할인 상품 조회 (할인율 있는 상품)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productDiscount > 0 ORDER BY p.productDiscount DESC")
    List<Product> findDiscountProducts(@Param("useYn") String useYn);
}