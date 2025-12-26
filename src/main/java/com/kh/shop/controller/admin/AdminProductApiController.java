package com.kh.shop.controller.admin;

import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductImage;
import com.kh.shop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/product")
public class AdminProductApiController {

    @Autowired
    private ProductService productService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        return loggedInUser != null;
    }

    @PostMapping("/create")
    public Map<String, Object> createProduct(
            @RequestParam String productName,
            @RequestParam Integer productPrice,
            @RequestParam(required = false, defaultValue = "0") Integer productDiscount,
            @RequestParam(required = false, defaultValue = "0") Integer productStock,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false, defaultValue = "0") Integer productOrder,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) List<MultipartFile> detailImages,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (productName == null || productName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "상품명을 입력하세요");
            return response;
        }

        if (productPrice == null || productPrice < 0) {
            response.put("success", false);
            response.put("message", "올바른 가격을 입력하세요");
            return response;
        }

        try {
            Product product = productService.createProduct(
                    productName, productPrice, productDiscount, productStock,
                    productDescription, productOrder, categoryId, thumbnail, detailImages);
            response.put("success", true);
            response.put("message", "상품이 등록되었습니다");
            response.put("productId", product.getProductId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/update/{productId}")
    public Map<String, Object> updateProduct(
            @PathVariable Long productId,
            @RequestParam String productName,
            @RequestParam Integer productPrice,
            @RequestParam(required = false, defaultValue = "0") Integer productDiscount,
            @RequestParam(required = false, defaultValue = "0") Integer productStock,
            @RequestParam(required = false) String productDescription,
            @RequestParam(required = false, defaultValue = "0") Integer productOrder,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) List<MultipartFile> detailImages,
            @RequestParam(required = false) List<Long> deleteImageIds,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        if (productName == null || productName.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "상품명을 입력하세요");
            return response;
        }

        Optional<Product> existingProduct = productService.getProductById(productId);
        if (!existingProduct.isPresent()) {
            response.put("success", false);
            response.put("message", "존재하지 않는 상품입니다");
            return response;
        }

        try {
            Product product = productService.updateProduct(
                    productId, productName, productPrice, productDiscount, productStock,
                    productDescription, productOrder, categoryId, thumbnail, detailImages, deleteImageIds);
            response.put("success", true);
            response.put("message", "상품이 수정되었습니다");
            response.put("productId", product.getProductId());
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            return response;
        }
    }

    @PostMapping("/delete/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable Long productId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        try {
            productService.deleteProduct(productId);
            response.put("success", true);
            response.put("message", "상품이 삭제되었습니다");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "상품 삭제 중 오류가 발생했습니다");
            return response;
        }
    }

    @GetMapping("/images/{productId}")
    public Map<String, Object> getProductImages(@PathVariable Long productId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        if (!isAdmin(session)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다");
            return response;
        }

        List<ProductImage> images = productService.getProductImages(productId);
        response.put("success", true);
        response.put("images", images);
        return response;
    }
}