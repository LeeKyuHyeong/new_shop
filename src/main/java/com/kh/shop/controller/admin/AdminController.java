package com.kh.shop.controller.admin;

import com.kh.shop.common.dto.PageRequestDTO;
import com.kh.shop.common.dto.PageResponseDTO;
import com.kh.shop.dto.UserSearchDTO;
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
    public String categoryList(HttpSession session, PageRequestDTO pageRequestDTO, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // 기본값 설정 추가
        if (pageRequestDTO.getSize() == 0) {
            pageRequestDTO.setSize(10);
        }
        if (pageRequestDTO.getPage() == 0) {
            pageRequestDTO.setPage(1);
        }

        // 페이징된 카테고리 목록 조회
        PageResponseDTO<Category> result = categoryService.getListWithPaging(pageRequestDTO);
        model.addAttribute("result", result);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        // 필터용 계층형 카테고리 (드롭다운용)
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
        if (category.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다. (ID: " + categoryId + ")");
        }
        model.addAttribute("category", category.get());
        // 상위 카테고리 목록 전달
        List<Category> parentCategories = categoryService.getParentCategories();
        model.addAttribute("parentCategories", parentCategories);
        return "admin/category/form";
    }

    // ==================== 상품 관리 ====================

    @GetMapping("/product")
    public String productList(com.kh.shop.common.dto.PageRequestDTO pageRequestDTO, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        // 기본값 설정
        if (pageRequestDTO.getSize() == 0) {
            pageRequestDTO.setSize(10);
        }
        if (pageRequestDTO.getPage() == 0) {
            pageRequestDTO.setPage(1);
        }

        PageResponseDTO<Product> result = productService.getProductListWithPaging(pageRequestDTO);
        model.addAttribute("result", result);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        // 카테고리 목록 (필터용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

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
        if (product.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. (ID: " + productId + ")");
        }
        model.addAttribute("product", product.get());
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);
        return "admin/product/form";
    }

    @GetMapping("/product/detail/{productId}")
    public String productDetailPage(@PathVariable Long productId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Optional<Product> product = productService.getProductById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. (ID: " + productId + ")");
        }
        model.addAttribute("product", product.get());
        return "admin/product/detail";
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
    public String userList(HttpSession session, UserSearchDTO searchDTO, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<User> users = userService.searchUsers(searchDTO);
        model.addAttribute("users", users);
        model.addAttribute("searchDTO", searchDTO);
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