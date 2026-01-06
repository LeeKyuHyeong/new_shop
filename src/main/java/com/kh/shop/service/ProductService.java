package com.kh.shop.service;

import com.kh.shop.common.dto.PageRequestDTO;
import com.kh.shop.common.dto.PageResponseDTO;
import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductImage;
import com.kh.shop.repository.CategoryRepository;
import com.kh.shop.repository.ProductImageRepository;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.util.ProfanityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProfanityFilter profanityFilter;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategory("Y");
    }

    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(categoryId, "Y");
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findByIdWithImages(productId);
    }

    @Transactional(readOnly = true)
    public List<Product> getNewProducts(int limit) {
        return productRepository.findNewProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Product> getBestProducts(int limit) {
        return productRepository.findBestProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Product> getDiscountProducts(int limit) {
        return productRepository.findDiscountProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Integer categoryId, Long excludeProductId, int limit) {
        return productRepository.findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(categoryId, "Y")
                .stream()
                .filter(p -> !p.getProductId().equals(excludeProductId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // ==================== 페이징 메서드 추가 ====================

    /**
     * Admin: 상품 목록 페이징 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<Product> getProductListWithPaging(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("productOrder");

        String searchType = pageRequestDTO.getSearchType();
        String searchKeyword = pageRequestDTO.getSearchKeyword();
        Integer categoryId = pageRequestDTO.getCategoryId();

        Page<Product> result;

        // 검색 조건에 따른 분기
        if (categoryId != null && searchKeyword != null && !searchKeyword.isEmpty()) {
            // 카테고리 + 검색어
            result = productRepository.findByProductNameAndCategoryPaging("Y", searchKeyword, categoryId, pageable);
        } else if (categoryId != null) {
            // 카테고리만
            result = productRepository.findByCategoryIdPaging("Y", categoryId, pageable);
        } else if (searchKeyword != null && !searchKeyword.isEmpty()) {
            // 검색어만
            result = productRepository.findByProductNameContaining("Y", searchKeyword, pageable);
        } else {
            // 전체
            result = productRepository.findAllWithCategoryPaging("Y", pageable);
        }

        return PageResponseDTO.<Product>withAll()
                .pageRequestDTO(pageRequestDTO)
                .result(result)
                .build();
    }

    /**
     * Client: 카테고리별 상품 페이징 조회
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<Product> getProductsByCategoryWithPaging(Integer categoryId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("productOrder");

        Page<Product> result = productRepository.findByCategoryPaging("Y", categoryId, pageable);

        return PageResponseDTO.<Product>withAll()
                .pageRequestDTO(pageRequestDTO)
                .result(result)
                .build();
    }

    /**
     * Client: 전체 상품 페이징 조회 (검색 포함)
     */
    @Transactional(readOnly = true)
    public PageResponseDTO<Product> getAllProductsWithPaging(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("createdDate");

        String searchKeyword = pageRequestDTO.getSearchKeyword();

        Page<Product> result;

        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            result = productRepository.findByKeywordPaging("Y", searchKeyword, pageable);
        } else {
            result = productRepository.findAllActivePaging("Y", pageable);
        }

        return PageResponseDTO.<Product>withAll()
                .pageRequestDTO(pageRequestDTO)
                .result(result)
                .build();
    }

    // ==================== 기존 CRUD 메서드들 유지 ====================

    @Transactional
    public Product createProduct(String productName, Integer productPrice, Integer productDiscount,
                                 Integer productStock, String productDescription, Integer productOrder,
                                 Integer categoryId, String color, String size, MultipartFile thumbnail,
                                 List<MultipartFile> detailImages) throws IOException {
        // ... 기존 코드 유지
        if (profanityFilter.containsProfanity(productName)) {
            List<String> detected = profanityFilter.detectProfanities(productName);
            throw new IllegalArgumentException("상품명에 부적절한 표현이 포함되어 있습니다: " + String.join(", ", detected));
        }

        if (productDescription != null && profanityFilter.containsProfanity(productDescription)) {
            List<String> detected = profanityFilter.detectProfanities(productDescription);
            throw new IllegalArgumentException("상품 설명에 부적절한 표현이 포함되어 있습니다: " + String.join(", ", detected));
        }

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = saveFile(thumbnail, "thumbnail");
        }

        Product product = Product.builder()
                .productName(productName)
                .productPrice(productPrice)
                .productDiscount(productDiscount)
                .productStock(productStock)
                .productDescription(productDescription)
                .productOrder(productOrder)
                .category(category)
                .thumbnailUrl(thumbnailUrl)
                .color(color)
                .size(size)
                .build();

        product = productRepository.save(product);

        if (detailImages != null && !detailImages.isEmpty()) {
            int order = 1;
            for (MultipartFile file : detailImages) {
                if (!file.isEmpty()) {
                    String imageUrl = saveFile(file, "detail");
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .imageOrder(order++)
                            .build();
                    productImageRepository.save(productImage);
                }
            }
        }

        return product;
    }

    @Transactional
    public Product updateProduct(Long productId, String productName, Integer productPrice, Integer productDiscount,
                                 Integer productStock, String productDescription, Integer productOrder,
                                 Integer categoryId, String color, String size, MultipartFile thumbnail,
                                 List<MultipartFile> detailImages, List<Long> deleteImageIds) throws IOException {
        // ... 기존 코드 유지
        if (profanityFilter.containsProfanity(productName)) {
            List<String> detected = profanityFilter.detectProfanities(productName);
            throw new IllegalArgumentException("상품명에 부적절한 표현이 포함되어 있습니다: " + String.join(", ", detected));
        }

        if (productDescription != null && profanityFilter.containsProfanity(productDescription)) {
            List<String> detected = profanityFilter.detectProfanities(productDescription);
            throw new IllegalArgumentException("상품 설명에 부적절한 표현이 포함되어 있습니다: " + String.join(", ", detected));
        }

        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            return null;
        }

        Product product = productOpt.get();

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        product.setProductName(productName);
        product.setProductPrice(productPrice);
        product.setProductDiscount(productDiscount);
        product.setProductStock(productStock);
        product.setProductDescription(productDescription);
        product.setProductOrder(productOrder);
        product.setCategory(category);
        product.setColor(color);
        product.setSize(size);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (product.getThumbnailUrl() != null) {
                deleteFile(product.getThumbnailUrl());
            }
            String thumbnailUrl = saveFile(thumbnail, "thumbnail");
            product.setThumbnailUrl(thumbnailUrl);
        }

        if (deleteImageIds != null && !deleteImageIds.isEmpty()) {
            for (Long imageId : deleteImageIds) {
                Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
                if (imageOpt.isPresent()) {
                    ProductImage image = imageOpt.get();
                    deleteFile(image.getImageUrl());
                    productImageRepository.delete(image);
                }
            }
        }

        if (detailImages != null && !detailImages.isEmpty()) {
            List<ProductImage> existingImages = productImageRepository.findByProductProductIdAndUseYnOrderByImageOrderAsc(productId, "Y");
            int order = existingImages.isEmpty() ? 1 : existingImages.get(existingImages.size() - 1).getImageOrder() + 1;

            for (MultipartFile file : detailImages) {
                if (!file.isEmpty()) {
                    String imageUrl = saveFile(file, "detail");
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imageUrl)
                            .imageOrder(order++)
                            .build();
                    productImageRepository.save(productImage);
                }
            }
        }

        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setUseYn("N");
            productRepository.save(product);
        }
    }

    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductProductIdAndUseYnOrderByImageOrderAsc(productId, "Y");
    }

    private String saveFile(MultipartFile file, String type) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = UUID.randomUUID().toString() + extension;

        String dirPath = uploadDir + "/" + type;
        File dir = new File(dirPath);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                throw new IOException("디렉토리 생성 실패: " + dirPath);
            }
        }

        File savedFile = new File(dirPath + "/" + savedFilename);
        file.transferTo(savedFile);

        return "/uploads/" + type + "/" + savedFilename;
    }

    private void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String filePath = uploadDir + fileUrl.substring("/uploads".length());
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    public Map<String, Object> validateProductContent(String productName, String productDescription) {
        Map<String, Object> result = new HashMap<>();
        boolean hasNameProfanity = profanityFilter.containsProfanity(productName);
        boolean hasDescProfanity = productDescription != null && profanityFilter.containsProfanity(productDescription);

        result.put("isValid", !hasNameProfanity && !hasDescProfanity);

        if (hasNameProfanity) {
            result.put("nameError", true);
            result.put("nameDetected", profanityFilter.detectProfanities(productName));
        }

        if (hasDescProfanity) {
            result.put("descError", true);
            result.put("descDetected", profanityFilter.detectProfanities(productDescription));
        }

        return result;
    }
}