package com.kh.shop.controller;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.ProductService;
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

    @Autowired
    private ProductService productService;

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

    // ==================== 상품 관리 ====================

    @GetMapping("/product")
    public String productList(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "admin/product/list";
    }

    @GetMapping("/product/create")
    public String productCreatePage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // 카테고리 목록 (상위 + 하위)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "admin/product/form";
    }

    @GetMapping("/product/edit/{productId}")
    public String productEditPage(@PathVariable Long productId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
            model.addAttribute("parentCategories", parentCategories);
            return "admin/product/form";
        }
        return "redirect:/admin/product";
    }

    @GetMapping("/product/detail/{productId}")
    public String productDetailPage(@PathVariable Long productId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Product> product = productService.getProductById(productId);
        if (product.isPresent()) {
            model.addAttribute("product", product.get());
            return "admin/product/detail";
        }
        return "redirect:/admin/product";
    }
}