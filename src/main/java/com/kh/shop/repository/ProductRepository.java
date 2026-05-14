package com.kh.shop.repository;

import com.kh.shop.entity.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByUseYnOrderByProductOrderAsc(String useYn);
    List<Product> findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(Integer categoryId, String useYn);

    // 연관 상품용: 카테고리 내에서 특정 productId 제외하고 N 개 - DB 단에서 LIMIT 적용.
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId "
            + "AND p.productId <> :excludeId AND p.useYn = :useYn "
            + "ORDER BY p.productOrder ASC")
    List<Product> findRelatedProducts(@Param("categoryId") Integer categoryId,
                                      @Param("excludeId") Long excludeId,
                                      @Param("useYn") String useYn,
                                      Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder ASC")
    List<Product> findAllWithCategory(@Param("useYn") String useYn);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images WHERE p.productId = :productId")
    Optional<Product> findByIdWithImages(@Param("productId") Long productId);

    Optional<Product> findByProductNameAndUseYn(String productName, String useYn);

    // 재고 차감/복구용 - PESSIMISTIC_WRITE 락으로 동시 트랜잭션 차단 (overselling 방지).
    // 호출 측에서 productId 오름차순으로 호출하여 데드락 회피할 것.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.productId = :productId")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.createdDate DESC")
    List<Product> findNewProducts(@Param("useYn") String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder DESC")
    List<Product> findBestProducts(@Param("useYn") String useYn);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productDiscount > 0 ORDER BY p.productDiscount DESC")
    List<Product> findDiscountProducts(@Param("useYn") String useYn);

    // 메모리 효율 변형 - DB 에서 LIMIT 으로 잘라 가져옴 (전체 로드 후 stream.limit 보다 빠르고 메모리 절약).
    // category 는 단일 ManyToOne 이므로 JOIN FETCH 와 Pageable 병행 안전.
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.createdDate DESC")
    List<Product> findNewProducts(@Param("useYn") String useYn, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn ORDER BY p.productOrder DESC")
    List<Product> findBestProducts(@Param("useYn") String useYn, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.productDiscount > 0 ORDER BY p.productDiscount DESC")
    List<Product> findDiscountProducts(@Param("useYn") String useYn, Pageable pageable);

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

    // Admin: 상위 카테고리로 필터링 (해당 상위 카테고리의 모든 하위 카테고리 상품)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.useYn = :useYn AND c.parent.categoryId = :parentCategoryId")
    Page<Product> findByParentCategoryIdPaging(@Param("useYn") String useYn, @Param("parentCategoryId") Integer parentCategoryId, Pageable pageable);

    // Admin: 상위 카테고리 + 검색어
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE p.useYn = :useYn AND c.parent.categoryId = :parentCategoryId AND p.productName LIKE %:keyword%")
    Page<Product> findByParentCategoryAndKeywordPaging(@Param("useYn") String useYn, @Param("parentCategoryId") Integer parentCategoryId, @Param("keyword") String keyword, Pageable pageable);

    // Client: 카테고리별 상품 페이징
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.category.categoryId = :categoryId")
    Page<Product> findByCategoryPaging(@Param("useYn") String useYn, @Param("categoryId") Integer categoryId, Pageable pageable);

    // Client: 전체 상품 페이징 (검색 포함)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND (p.productName LIKE %:keyword% OR p.productDescription LIKE %:keyword%)")
    Page<Product> findByKeywordPaging(@Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

    // Client: 전체 상품 페이징
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn")
    Page<Product> findAllActivePaging(@Param("useYn") String useYn, Pageable pageable);

    // 베스트 상품 조회 (bestRank 기준)
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.useYn = :useYn AND p.bestRank IS NOT NULL ORDER BY p.bestRank ASC")
    List<Product> findBestRankedProducts(@Param("useYn") String useYn);

    // 베스트 순위 초기화
    @Modifying
    @Query("UPDATE Product p SET p.bestRank = NULL WHERE p.useYn = 'Y'")
    int clearAllBestRank();

    // 재고 부족 상품 조회 (재고 N개 이하)
    @Query("SELECT p FROM Product p WHERE p.useYn = 'Y' AND p.productStock <= :threshold AND p.productStock > 0 ORDER BY p.productStock ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    // 품절 상품 조회
    @Query("SELECT p FROM Product p WHERE p.useYn = 'Y' AND p.productStock = 0")
    List<Product> findOutOfStockProducts();

    // 최근 재입고된 상품 조회 (이전에 품절이었다가 재고가 생긴 상품)
    @Query("SELECT p FROM Product p WHERE p.useYn = 'Y' AND p.productStock > 0 AND p.updatedDate >= :since")
    List<Product> findRecentlyRestockedProducts(@Param("since") java.time.LocalDateTime since);

    // 할인 시작된 상품 조회 (최근 N시간 내 할인율이 0보다 커진 상품)
    @Query("SELECT p FROM Product p WHERE p.useYn = 'Y' AND p.productDiscount > 0 AND p.updatedDate >= :since")
    List<Product> findRecentlyDiscountedProducts(@Param("since") java.time.LocalDateTime since);

}