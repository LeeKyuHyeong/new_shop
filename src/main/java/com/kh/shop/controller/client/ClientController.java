package com.kh.shop.controller.client;

import com.kh.shop.common.dto.PageRequestDTO;
import com.kh.shop.common.dto.PageResponseDTO;
import com.kh.shop.entity.Category;
import com.kh.shop.entity.Popup;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.Slide;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.PopupService;
import com.kh.shop.service.ProductService;
import com.kh.shop.service.SiteSettingService;
import com.kh.shop.service.SlideService;
import com.kh.shop.service.WishlistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private PopupService popupService;

    @Autowired
    private WishlistService wishlistService;

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

        // 활성화된 팝업 조회
        List<Popup> activePopups = popupService.getActivePopups();
        model.addAttribute("popups", activePopups);

        // 팝업 지속시간 (사이트 설정)
        int popupDuration = siteSettingService.getPopupDuration();
        model.addAttribute("popupDuration", popupDuration);

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

    // 카테고리별 상품 목록 (페이징 추가)
    @GetMapping("/category/{categoryId}")
    public String categoryPage(@PathVariable Long categoryId,
                               PageRequestDTO pageRequestDTO,
                               Model model) {
        // 선택된 카테고리 정보
        Optional<Category> categoryOpt = categoryService.getCategoryById(categoryId.intValue());
        if (categoryOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다. (ID: " + categoryId + ")");
        }

        Category selectedCategory = categoryOpt.get();

        // 상위 카테고리인 경우 (parent가 null) 첫 번째 하위 카테고리로 리다이렉트
        if (selectedCategory.getParent() == null && !selectedCategory.getChildren().isEmpty()) {
            Category firstChild = selectedCategory.getChildren().get(0);
            return "redirect:/category/" + firstChild.getCategoryId();
        }

        // 상위 카테고리와 하위 카테고리 조회 (헤더 메뉴용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        model.addAttribute("selectedCategory", selectedCategory);
        model.addAttribute("selectedCategoryId", categoryId);

        // 부모 카테고리 ID (하위 카테고리 선택 시 상위 메뉴 활성화용)
        if (selectedCategory.getParent() != null) {
            model.addAttribute("selectedParentId", selectedCategory.getParent().getCategoryId());
        }

        // 기본값 설정
        if (pageRequestDTO.getSize() == 0) {
            pageRequestDTO.setSize(12);
        }
        if (pageRequestDTO.getPage() == 0) {
            pageRequestDTO.setPage(1);
        }

        // 해당 카테고리의 상품 목록 페이징 조회
        pageRequestDTO.setCategoryId(categoryId.intValue());
        PageResponseDTO<Product> result = productService.getProductsByCategoryWithPaging(
                categoryId.intValue(), pageRequestDTO);
        model.addAttribute("result", result);
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        return "client/main";
    }

    // 상품 상세 페이지
    @GetMapping("/product/{productId}")
    public String productDetail(@PathVariable Long productId, Model model) {
        // 상위 카테고리와 하위 카테고리 조회 (헤더 메뉴용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        // 상품 상세 정보
        Optional<Product> productOpt = productService.getProductById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다. (ID: " + productId + ")");
        }

        Product product = productOpt.get();
        model.addAttribute("product", product);

        // 선택된 카테고리 정보 (브레드크럼용)
        if (product.getCategory() != null) {
            model.addAttribute("selectedCategoryId", product.getCategory().getCategoryId());
            if (product.getCategory().getParent() != null) {
                model.addAttribute("selectedParentId", product.getCategory().getParent().getCategoryId());
            }
        }

        // 관련 상품 (같은 카테고리의 다른 상품)
        if (product.getCategory() != null) {
            List<Product> relatedProducts = productService.getRelatedProducts(
                    product.getCategory().getCategoryId(),
                    productId,
                    4
            );
            model.addAttribute("relatedProducts", relatedProducts);
        }

        return "client/product-detail";
    }

    // 위시리스트 페이지
    @GetMapping("/wishlist")
    public String wishlistPage(Model model, HttpSession session) {
        // 상위 카테고리와 하위 카테고리 조회 (헤더 메뉴용)
        List<Category> parentCategories = categoryService.getParentCategoriesWithChildren();
        model.addAttribute("parentCategories", parentCategories);

        // 로그인 확인
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login?redirect=/wishlist";
        }

        return "client/wishlist";
    }
}