package com.kh.shop.repository;

import com.kh.shop.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUseYnOrderByProductOrderAsc(String useYn);
    List<Product> findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(Integer categoryId, String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder ASC")
    List<Product> findAllWithCategory(@Param("useYn") String useYn);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") Long productId);

    Optional<Product> findByProductNameAndUseYn(String productName, String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.createdDate DESC")
    List<Product> findNewProducts(@Param("useYn") String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder DESC")
    List<Product> findBestProducts(@Param("useYn") String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productDiscount > 0 ORDER BY p.productDiscount DESC")
    List<Product> findDiscountProducts(@Param("useYn") String useYn);

    // ==================== 페이징 메서드 추가 ====================

    // Admin: 전체 상품 페이징 조회
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn")
    Page<Product> findAllWithCategoryPaging(@Param("useYn") String useYn, Pageable pageable);

    // Admin: 검색 (상품명)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productName LIKE %:keyword%")
    Page<Product> findByProductNameContaining(@Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

    // Admin: 검색 (카테고리별)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.category.categoryId = :categoryId")
    Page<Product> findByCategoryIdPaging(@Param("useYn") String useYn, @Param("categoryId") Integer categoryId, Pageable pageable);

    // Admin: 검색 (상품명 + 카테고리)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productName LIKE %:keyword% AND p.category.categoryId = :categoryId")
    Page<Product> findByProductNameAndCategoryPaging(@Param("useYn") String useYn, @Param("keyword") String keyword, @Param("categoryId") Integer categoryId, Pageable pageable);

    // Client: 카테고리별 상품 페이징
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.category.categoryId = :categoryId")
    Page<Product> findByCategoryPaging(@Param("useYn") String useYn, @Param("categoryId") Integer categoryId, Pageable pageable);

    // Client: 전체 상품 페이징 (검색 포함)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND (p.productName LIKE %:keyword% OR p.productDescription LIKE %:keyword%)")
    Page<Product> findByKeywordPaging(@Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

    // Client: 전체 상품 페이징
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn")
    Page<Product> findAllActivePaging(@Param("useYn") String useYn, Pageable pageable);
}