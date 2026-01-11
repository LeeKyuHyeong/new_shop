package com.kh.shop.service;

import com.kh.shop.common.dto.PageRequestDTO;
import com.kh.shop.common.dto.PageResponseDTO;
import com.kh.shop.entity.Category;
import com.kh.shop.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    public List<Category> getAllCategories() {
        return categoryRepository.findByUseYnOrderByCategoryOrder("Y");
    }

    // 상위 카테고리만 조회
    public List<Category> getParentCategories() {
        return categoryRepository.findByParentIsNullAndUseYnOrderByCategoryOrder("Y");
    }

    // 상위 카테고리 + 하위 카테고리 함께 조회 (계층형)
    @Transactional(readOnly = true)
    public List<Category> getParentCategoriesWithChildren() {
        return categoryRepository.findParentCategoriesWithChildren("Y");
    }

    // 특정 상위 카테고리의 하위 카테고리 조회
    public List<Category> getChildCategories(Integer parentId) {
        return categoryRepository.findByParentCategoryIdAndUseYnOrderByCategoryOrder(parentId, "Y");
    }

    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }

    @Transactional
    public Category createCategory(String categoryName, String categoryDescription, Integer categoryOrder, Integer parentId) {
        Category parent = null;
        if (parentId != null) {
            parent = categoryRepository.findById(parentId).orElse(null);
        }

        Category category = Category.builder()
                .categoryName(categoryName)
                .categoryDescription(categoryDescription)
                .categoryOrder(categoryOrder != null ? categoryOrder : 0)
                .parent(parent)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Integer categoryId, String categoryName, String categoryDescription, Integer categoryOrder, Integer parentId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setCategoryName(categoryName);
            category.setCategoryDescription(categoryDescription);
            category.setCategoryOrder(categoryOrder != null ? categoryOrder : 0);

            if (parentId != null) {
                Category parent = categoryRepository.findById(parentId).orElse(null);
                category.setParent(parent);
            } else {
                category.setParent(null);
            }

            return categoryRepository.save(category);
        }
        return null;
    }

    @Transactional
    public void deleteCategory(Integer categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();
            category.setUseYn("N");

            List<Category> children = categoryRepository.findByParentCategoryIdAndUseYnOrderByCategoryOrder(categoryId, "Y");
            for (Category child : children) {
                child.setUseYn("N");
                categoryRepository.save(child);
            }

            categoryRepository.save(category);
        }
    }

    public boolean isDuplicateCategoryName(String categoryName, Integer parentId) {
        return categoryRepository.findByCategoryNameAndParentId(categoryName, parentId).isPresent();
    }

    public boolean isDuplicateCategoryName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName).isPresent();
    }

    /**
     * 카테고리 목록 페이징 조회 (플랫 목록 + 상위카테고리 필터)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<Category> getListWithPaging(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("categoryOrder");

        String searchKeyword = pageRequestDTO.getSearchKeyword();
        Integer categoryId = pageRequestDTO.getCategoryId();
        Integer parentCategoryId = pageRequestDTO.getParentCategoryId();

        Page<Category> result;

        // 조건 분기
        if (categoryId != null) {
            // 특정 카테고리만 조회
            result = categoryRepository.findByCategoryIdPaging("Y", categoryId, pageable);
        } else if (parentCategoryId != null) {
            // 상위 카테고리로 필터링 (하위 카테고리만)
            result = categoryRepository.findByParentCategoryIdPaging("Y", parentCategoryId, pageable);
        } else if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // 카테고리명 검색
            result = categoryRepository.findByCategoryNameContainingWithParent("Y", searchKeyword, pageable);
        } else {
            // 전체 조회 (플랫 목록)
            result = categoryRepository.findAllFlatWithParent("Y", pageable);
        }

        return PageResponseDTO.<Category>withAll()
                .pageRequestDTO(pageRequestDTO)
                .result(result)
                .build();
    }
}