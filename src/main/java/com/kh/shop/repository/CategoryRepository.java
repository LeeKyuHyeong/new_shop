package com.kh.shop.repository;

import com.kh.shop.entity.Category;
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
}