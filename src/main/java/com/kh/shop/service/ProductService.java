package com.kh.shop.service;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductImage;
import com.kh.shop.repository.CategoryRepository;
import com.kh.shop.repository.ProductImageRepository;
import com.kh.shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
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

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // 전체 상품 조회
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findAllWithCategory("Y");
    }

    // 카테고리별 상품 조회
    public List<Product> getProductsByCategory(Integer categoryId) {
        return productRepository.findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(categoryId, "Y");
    }

    // 상품 상세 조회 (이미지 포함)
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long productId) {
        return productRepository.findByIdWithImages(productId);
    }

    // 신상품 조회 (최신 10개)
    @Transactional(readOnly = true)
    public List<Product> getNewProducts(int limit) {
        return productRepository.findNewProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 베스트 상품 조회 (상위 10개)
    @Transactional(readOnly = true)
    public List<Product> getBestProducts(int limit) {
        return productRepository.findBestProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 할인 상품 조회 (상위 10개)
    @Transactional(readOnly = true)
    public List<Product> getDiscountProducts(int limit) {
        return productRepository.findDiscountProducts("Y").stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 관련 상품 조회 (같은 카테고리의 다른 상품)
    @Transactional(readOnly = true)
    public List<Product> getRelatedProducts(Integer categoryId, Long excludeProductId, int limit) {
        return productRepository.findByCategoryCategoryIdAndUseYnOrderByProductOrderAsc(categoryId, "Y")
                .stream()
                .filter(p -> !p.getProductId().equals(excludeProductId))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // 상품 등록
    @Transactional
    public Product createProduct(String productName, Integer productPrice, Integer productDiscount,
                                 Integer productStock, String productDescription, Integer productOrder,
                                 Integer categoryId, MultipartFile thumbnail, List<MultipartFile> detailImages) throws IOException {

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId).orElse(null);
        }

        // 썸네일 저장
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
                .build();

        product = productRepository.save(product);

        // 상세 이미지 저장
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

    // 상품 수정
    @Transactional
    public Product updateProduct(Long productId, String productName, Integer productPrice, Integer productDiscount,
                                 Integer productStock, String productDescription, Integer productOrder,
                                 Integer categoryId, MultipartFile thumbnail, List<MultipartFile> detailImages,
                                 List<Long> deleteImageIds) throws IOException {

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

        // 썸네일 변경
        if (thumbnail != null && !thumbnail.isEmpty()) {
            // 기존 썸네일 삭제
            if (product.getThumbnailUrl() != null) {
                deleteFile(product.getThumbnailUrl());
            }
            String thumbnailUrl = saveFile(thumbnail, "thumbnail");
            product.setThumbnailUrl(thumbnailUrl);
        }

        // 삭제할 이미지 처리
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

        // 새 상세 이미지 추가
        if (detailImages != null && !detailImages.isEmpty()) {
            // 현재 최대 순서 구하기
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

    // 상품 삭제 (soft delete)
    @Transactional
    public void deleteProduct(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setUseYn("N");
            productRepository.save(product);
        }
    }

    // 상품 이미지 조회
    public List<ProductImage> getProductImages(Long productId) {
        return productImageRepository.findByProductProductIdAndUseYnOrderByImageOrderAsc(productId, "Y");
    }

    // 파일 저장
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

    // 파일 삭제
    private void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            String filePath = uploadDir + fileUrl.substring("/uploads".length());
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        }
    }
}