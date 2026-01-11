package com.kh.shop.repository;

import com.kh.shop.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Optional<Category> findByCategoryName(String categoryName);

    List<Category> findByUseYnOrderByCategoryOrder(String useYn);

    // 상위 카테고리만 조회 (parent가 null인 것)
    List<Category> findByParentIsNullAndUseYnOrderByCategoryOrder(String useYn);

    // 특정 상위 카테고리의 하위 카테고리 조회
    List<Category> findByParentCategoryIdAndUseYnOrderByCategoryOrder(Integer parentId, String useYn);

    // 상위 카테고리와 하위 카테고리 함께 조회 (fetch join)
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL AND c.useYn = :useYn ORDER BY c.categoryOrder")
    List<Category> findParentCategoriesWithChildren(@Param("useYn") String useYn);

    // 중복 체크 (같은 상위 카테고리 내에서 이름 중복)
    @Query("SELECT c FROM Category c WHERE c.categoryName = :name AND ((:parentId IS NULL AND c.parent IS NULL) OR c.parent.categoryId = :parentId)")
    Optional<Category> findByCategoryNameAndParentId(@Param("name") String categoryName, @Param("parentId") Integer parentId);

    // ==================== 페이징 메서드 ====================

    // 전체 카테고리 페이징 조회
    @Query("SELECT c FROM Category c WHERE c.useYn = :useYn")
    Page<Category> findAllWithCategoryPaging(@Param("useYn") String useYn, Pageable pageable);

    // 특정 카테고리 ID로 조회
    @Query("SELECT c FROM Category c WHERE c.useYn = :useYn AND c.categoryId = :categoryId")
    Page<Category> findByCategoryIdPaging(@Param("useYn") String useYn, @Param("categoryId") Integer categoryId, Pageable pageable);

    // 카테고리명 검색 (추가)
    @Query("SELECT c FROM Category c WHERE c.useYn = :useYn AND c.categoryName LIKE %:keyword%")
    Page<Category> findByCategoryNameContaining(@Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);

    // 상위 카테고리로 필터링 (해당 상위 카테고리의 하위 카테고리들만 조회)
    @Query("SELECT c FROM Category c WHERE c.useYn = :useYn AND c.parent.categoryId = :parentId")
    Page<Category> findByParentCategoryIdPaging(@Param("useYn") String useYn, @Param("parentId") Integer parentId, Pageable pageable);

    // 전체 카테고리 플랫 목록 조회 (상위 카테고리 포함 - LEFT JOIN)
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.useYn = :useYn")
    Page<Category> findAllFlatWithParent(@Param("useYn") String useYn, Pageable pageable);

    // 카테고리명 검색 + 상위 카테고리 포함
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.parent WHERE c.useYn = :useYn AND c.categoryName LIKE %:keyword%")
    Page<Category> findByCategoryNameContainingWithParent(@Param("useYn") String useYn, @Param("keyword") String keyword, Pageable pageable);
}