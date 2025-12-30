package com.kh.shop.controller.admin;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.Slide;
import com.kh.shop.entity.User;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.ProductService;
import com.kh.shop.service.SlideService;
import com.kh.shop.service.SiteSettingService;
import com.kh.shop.service.UserService;
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

    @Autowired
    private SlideService slideService;

    @Autowired
    private SiteSettingService siteSettingService;

    @Autowired
    private UserService userService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        return loggedInUser != null && "ADMIN".equals(userRole);
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

    // ==================== 슬라이드 관리 ====================

    @GetMapping("/slide")
    public String slideList(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Slide> slides = slideService.getAllSlides();
        model.addAttribute("slides", slides);
        return "admin/slide/list";
    }

    @GetMapping("/slide/create")
    public String slideCreatePage(HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }
        return "admin/slide/form";
    }

    @GetMapping("/slide/edit/{slideId}")
    public String slideEditPage(@PathVariable Long slideId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Slide> slide = slideService.getSlideById(slideId);
        if (slide.isPresent()) {
            model.addAttribute("slide", slide.get());
            return "admin/slide/form";
        }
        return "redirect:/admin/slide";
    }

    // ==================== 사이트 설정 ====================

    @GetMapping("/setting")
    public String settingPage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // 초기 설정이 없으면 생성
        siteSettingService.initDefaultSettings();

        String siteName = siteSettingService.getSettingValue(SiteSettingService.KEY_SITE_NAME, "KH SHOP");
        int slideDuration = siteSettingService.getSlideDuration();
        int popupDuration = siteSettingService.getPopupDuration();

        model.addAttribute("siteName", siteName);
        model.addAttribute("popupDuration", popupDuration);
        model.addAttribute("slideDuration", slideDuration);
        return "admin/setting/index";
    }

    // ==================== 사용자 관리 ====================

    @GetMapping("/user")
    public String userList(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/user/list";
    }

    @GetMapping("/user/detail/{userId}")
    public String userDetailPage(@PathVariable String userId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<User> user = userService.getUserByUserId(userId);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "admin/user/detail";
        }
        return "redirect:/admin/user";
    }
}