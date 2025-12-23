package com.kh.shop.controller;

import com.kh.shop.entity.Category;
import com.kh.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @GetMapping("")
    public String adminDashboard(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin/index";
    }

    @GetMapping("/category")
    public String categoryList(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // 계층형 카테고리 조회
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "admin/category/list";
    }

    @GetMapping("/category/create")
    public String categoryCreatePage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        // 상위 카테고리 목록 전달
        List<Category> parentCategories = categoryService.getParentCategories();
        model.addAttribute("parentCategories", parentCategories);
        return "admin/category/form";
    }

    @GetMapping("/category/edit/{categoryId}")
    public String categoryEditPage(@PathVariable Integer categoryId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Category> category = categoryService.getCategoryById(categoryId);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            // 상위 카테고리 목록 전달
            List<Category> parentCategories = categoryService.getParentCategories();
            model.addAttribute("parentCategories", parentCategories);
            return "admin/category/form";
        }
        return "redirect:/admin/category";
    }
}