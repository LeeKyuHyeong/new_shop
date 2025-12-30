package com.kh.shop.controller.client;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Popup;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.Slide;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.PopupService;
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

    @Autowired
    private PopupService popupService;

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

    // ... 나머지 메서드는 동일 ...
}