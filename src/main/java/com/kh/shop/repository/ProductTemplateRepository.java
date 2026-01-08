package com.kh.shop.repository;

import com.kh.shop.entity.ProductTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long> {

    // 카테고리 + 타입별 템플릿 조회
    List<ProductTemplate> findByCategoryCategoryIdAndTemplateTypeAndUseYn(
            Integer categoryId, String templateType, String useYn);

    // 카테고리의 모든 템플릿 조회
    List<ProductTemplate> findByCategoryCategoryIdAndUseYn(Integer categoryId, String useYn);

    // 특정 타입의 템플릿 존재 여부
    boolean existsByCategoryCategoryIdAndTemplateTypeAndUseYn(
            Integer categoryId, String templateType, String useYn);

    // 카테고리별 템플릿 개수
    @Query("SELECT COUNT(t) FROM ProductTemplate t WHERE t.category.categoryId = :categoryId AND t.useYn = 'Y'")
    long countByCategoryId(@Param("categoryId") Integer categoryId);
}