package com.kh.shop.controller;

import com.kh.shop.entity.Category;
import com.kh.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/category")
public class CategoryApiController {

    @Autowired
    private CategoryService categoryService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/create")
    public Map<String, Object> createCategory(@RequestParam String categoryName,
                                              @RequestParam(required = false) String categoryDescription,
                                              @RequestParam(required = false, defaultValue = "0") Integer categoryOrder,
                                              @RequestParam(required = false) Integer parentId,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (categoryName == null || categoryName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "카테고리명을 입력하세요");
            return response;
        }

        // 같은 상위 카테고리 내에서 중복 체크
        if (categoryService.isDuplicateCategoryName(categoryName, parentId)) {
            response.put("success", false);
            response.put("message", "이미 존재하는 카테고리명입니다");
            return response;
        }

        try {
            Category category = categoryService.createCategory(categoryName, categoryDescription, categoryOrder, parentId);
            response.put("success", true);
            response.put("message", "카테고리가 등록되었습니다");
            response.put("categoryId", category.getCategoryId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 등록 중 오류가 발생했습니다");
            return response;
        }
    }

    @PostMapping("/update/{categoryId}")
    public Map<String, Object> updateCategory(@PathVariable Integer categoryId,
                                              @RequestParam String categoryName,
                                              @RequestParam(required = false) String categoryDescription,
                                              @RequestParam(required = false, defaultValue = "0") Integer categoryOrder,
                                              @RequestParam(required = false) Integer parentId,
                                              HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (categoryName == null || categoryName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "카테고리명을 입력하세요");
            return response;
        }

        Optional<Category> existingCategory = categoryService.getCategoryById(categoryId);
        if (!existingCategory.isPresent()) {
            response.put("success", false);
            response.put("message", "존재하지 않는 카테고리입니다");
            return response;
        }

        // 이름이 변경되었고, 같은 상위 카테고리 내에서 중복인 경우
        Category current = existingCategory.get();
        boolean nameChanged = !current.getCategoryName().equals(categoryName);
        Integer currentParentId = current.getParentId(categoryId);
        boolean parentChanged = (parentId == null && currentParentId != null) ||
                (parentId != null && !parentId.equals(currentParentId));

        if ((nameChanged || parentChanged) && categoryService.isDuplicateCategoryName(categoryName, parentId)) {
            response.put("success", false);
            response.put("message", "이미 존재하는 카테고리명입니다");
            return response;
        }

        try {
            Category category = categoryService.updateCategory(categoryId, categoryName, categoryDescription, categoryOrder, parentId);
            response.put("success", true);
            response.put("message", "카테고리가 수정되었습니다");
            response.put("categoryId", category.getCategoryId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 수정 중 오류가 발생했습니다");
            return response;
        }
    }

    @PostMapping("/delete/{categoryId}")
    public Map<String, Object> deleteCategory(@PathVariable Integer categoryId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        try {
            categoryService.deleteCategory(categoryId);
            response.put("success", true);
            response.put("message", "카테고리가 삭제되었습니다");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "카테고리 삭제 중 오류가 발생했습니다");
            return response;
        }
    }

    // 하위 카테고리 목록 조회 API
    @GetMapping("/children/{parentId}")
    public Map<String, Object> getChildCategories(@PathVariable Integer parentId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        List<Category> children = categoryService.getChildCategories(parentId);
        response.put("success", true);
        response.put("children", children);
        return response;
    }
}