package com.kh.shop.controller.client;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.Slide;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.ProductService;
import com.kh.shop.service.SiteSettingService;
import com.kh.shop.service.SlideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class ClientController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SlideService slideService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SiteSettingService siteSettingService;

    @GetMapping("/")
    public String clientMain(Model model) {
        // 상위 카테고리와 하위 카테고리 조회
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        // 활성화된 슬라이드 조회
        List<Slide> activeSlides = slideService.getActiveSlides();
        model.addAttribute("slides", activeSlides);

        // 슬라이드 지속시간 (사이트 설정)
        int slideDuration = siteSettingService.getSlideDuration();
        model.addAttribute("slideDuration", slideDuration);

        // 신상품 조회 (최신 8개)
        List<Product> newProducts = productService.getNewProducts(8);
        model.addAttribute("newProducts", newProducts);

        // 베스트 상품 조회 (상위 8개)
        List<Product> bestProducts = productService.getBestProducts(8);
        model.addAttribute("bestProducts", bestProducts);

        // 할인 상품 조회 (상위 8개)
        List<Product> discountProducts = productService.getDiscountProducts(8);
        model.addAttribute("discountProducts", discountProducts);

        return "client/main";
    }

    @GetMapping("/client")
    public String clientRedirect() {
        return "redirect:/";
    }

    @GetMapping("/category/{categoryId}")
    public String categoryProducts(@PathVariable Integer categoryId, Model model) {
        // 선택된 카테고리 정보 조회
        Category selectedCategory = categoryService.getCategoryById(categoryId).orElse(null);

        if (selectedCategory == null) {
            return "redirect:/";
        }

        // 상위 카테고리인 경우 첫 번째 하위 카테고리로 리다이렉트
        if (selectedCategory.getParent() == null) {
            List<Category> children = categoryService.getChildCategories(categoryId);
            if (!children.isEmpty()) {
                return "redirect:/category/" + children.get(0).getCategoryId();
            }
        }

        // 상위 카테고리와 하위 카테고리 조회 (메뉴용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedCategoryId", categoryId);

        // 부모 카테고리 ID 전달 (메뉴 active 표시용)
        if (selectedCategory.getParent() != null) {
            model.addAttribute("selectedParentId", selectedCategory.getParent().getCategoryId());
        }

        // 해당 카테고리의 상품 조회
        List<Product> products = productService.getProductsByCategory(categoryId);
        model.addAttribute("products", products);

        return "client/main";
    }

    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        // 상품 조회
        Product product = productService.getProductById(productId).orElse(null);

        if (product == null) {
            return "redirect:/";
        }

        // 상위 카테고리와 하위 카테고리 조회 (메뉴용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        model.addAttribute("product", product);

        // 카테고리 정보 전달 (메뉴 active 표시용)
        if (product.getCategory() != null) {
            model.addAttribute("selectedCategoryId", product.getCategory().getCategoryId());
            if (product.getCategory().getParent() != null) {
                model.addAttribute("selectedParentId", product.getCategory().getParent().getCategoryId());
            }
        }

        // 관련 상품 조회 (같은 카테고리의 다른 상품)
        if (product.getCategory() != null) {
            List<Product> relatedProducts = productService.getRelatedProducts(
                    product.getCategory().getCategoryId(), productId, 4);
            model.addAttribute("relatedProducts", relatedProducts);
        }

        return "client/product-detail";
    }
}