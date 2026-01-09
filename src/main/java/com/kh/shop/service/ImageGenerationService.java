package com.kh.shop.service;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductImage;
import com.kh.shop.repository.ProductImageRepository;
import com.kh.shop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageGenerationService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${image.upload.path:/root/new_shop/uploads}")
    private String uploadPath;

    @Value("${image.generation.enabled:false}")
    private boolean generationEnabled;

    @Value("${image.generation.daily-limit:40}")
    private int dailyLimit;

    // 일일 생성 카운터 (메모리 기반, 서버 재시작 시 초기화)
    private int dailyGeneratedCount = 0;
    private LocalDate lastResetDate = LocalDate.now();

    // 이미지 생성 모델 (무료 티어 지원)
    private static final String IMAGE_MODEL = "gemini-2.0-flash-exp-image-generation";

    /**
     * Google GenAI Client 생성 (매 요청마다 새로 생성)
     */
    private Client createClient() {
        if (!generationEnabled || geminiApiKey.isEmpty()) {
            return null;
        }
        try {
            return Client.builder()
                    .apiKey(geminiApiKey)
                    .build();
        } catch (Exception e) {
            log.error("[이미지생성] Google GenAI Client 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 이미지 없는 상품들에 대해 이미지 생성 (일일 제한 적용)
     */
    @Transactional
    public Map<String, Object> generateImagesForProductsWithoutImages(int maxProducts) {
        Map<String, Object> result = new HashMap<>();

        // 일일 카운터 리셋 체크
        resetDailyCounterIfNeeded();

        // 남은 일일 할당량 계산
        int remainingQuota = dailyLimit - dailyGeneratedCount;
        int imagesPerProduct = 4; // 썸네일 1 + 상세이미지 3
        int availableProducts = remainingQuota / imagesPerProduct;

        if (availableProducts <= 0) {
            result.put("success", false);
            result.put("message", "일일 이미지 생성 한도(" + dailyLimit + "장)에 도달했습니다. 내일 다시 시도해주세요.");
            result.put("dailyUsed", dailyGeneratedCount);
            result.put("dailyLimit", dailyLimit);
            return result;
        }

        // 실제 처리할 상품 수 결정
        int productsToProcess = Math.min(maxProducts, availableProducts);

        // 이미지 없는 상품 조회
        List<Product> products = productRepository.findProductsWithoutImages(productsToProcess);

        if (products.isEmpty()) {
            result.put("success", true);
            result.put("message", "이미지가 없는 상품이 없습니다.");
            result.put("processedCount", 0);
            return result;
        }

        int successCount = 0;
        int failCount = 0;
        List<String> processedProducts = new ArrayList<>();
        List<String> failedProducts = new ArrayList<>();

        for (Product product : products) {
            try {
                boolean generated = generateImagesForProduct(product);
                if (generated) {
                    successCount++;
                    dailyGeneratedCount += imagesPerProduct;
                    processedProducts.add(product.getProductName());
                } else {
                    failCount++;
                    failedProducts.add(product.getProductName());
                }

                // API 호출 간 딜레이 (Rate Limit 방지)
                Thread.sleep(2000);

            } catch (Exception e) {
                log.error("[이미지생성] 상품 {} 처리 실패: {}", product.getProductId(), e.getMessage());
                failCount++;
                failedProducts.add(product.getProductName());
            }
        }

        result.put("success", true);
        result.put("message", String.format("%d개 상품 이미지 생성 완료 (성공: %d, 실패: %d)",
                products.size(), successCount, failCount));
        result.put("processedCount", successCount);
        result.put("failedCount", failCount);
        result.put("processedProducts", processedProducts);
        result.put("failedProducts", failedProducts);
        result.put("dailyUsed", dailyGeneratedCount);
        result.put("dailyLimit", dailyLimit);
        result.put("remainingQuota", dailyLimit - dailyGeneratedCount);

        return result;
    }

    /**
     * 특정 상품에 대해 이미지 생성
     */
    @Transactional
    public boolean generateImagesForProduct(Product product) {
        if (!generationEnabled || geminiApiKey.isEmpty()) {
            log.warn("[이미지생성] API가 비활성화되어 있거나 API 키가 없습니다. 플레이스홀더 생성.");
            return generatePlaceholderImages(product);
        }

        try (Client client = createClient()) {
            if (client == null) {
                log.warn("[이미지생성] Client 생성 실패. 플레이스홀더 생성.");
                return generatePlaceholderImages(product);
            }

            String categoryName = product.getCategory() != null ?
                    product.getCategory().getCategoryName() : "상품";

            // 상품 디렉토리 생성
            String productDir = uploadPath + "/products/" + product.getProductId();
            Files.createDirectories(Paths.get(productDir));

            // 1. 썸네일 생성
            String thumbnailPrompt = buildImagePrompt(product.getProductName(), categoryName, "thumbnail");
            String thumbnailPath = generateAndSaveImage(client, thumbnailPrompt, productDir, "thumbnail.png");

            if (thumbnailPath != null) {
                product.setThumbnailUrl("/uploads/products/" + product.getProductId() + "/thumbnail.png");
                productRepository.save(product);
            }

            // 2. 상세 이미지 3장 생성
            for (int i = 1; i <= 3; i++) {
                String detailPrompt = buildImagePrompt(product.getProductName(), categoryName, "detail" + i);
                String detailPath = generateAndSaveImage(client, detailPrompt, productDir, "detail_" + i + ".png");

                if (detailPath != null) {
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl("/uploads/products/" + product.getProductId() + "/detail_" + i + ".png")
                            .imageOrder(i)
                            .build();
                    productImageRepository.save(productImage);
                }

                // API 호출 간 딜레이
                Thread.sleep(1500);
            }

            log.info("[이미지생성] 상품 {} ({}) 이미지 생성 완료", product.getProductId(), product.getProductName());
            return true;

        } catch (Exception e) {
            log.error("[이미지생성] 상품 {} 이미지 생성 실패: {}", product.getProductId(), e.getMessage());
            // 실패 시 플레이스홀더로 대체
            return generatePlaceholderImages(product);
        }
    }

    /**
     * Google GenAI SDK를 사용하여 이미지 생성 및 저장
     */
    private String generateAndSaveImage(Client client, String prompt, String directory, String filename) {
        try {
            log.info("[이미지생성] 이미지 생성 요청: {}", filename);

            // GenerateContentConfig 설정 - TEXT와 IMAGE 모두 응답으로 받기
            GenerateContentConfig config = GenerateContentConfig.builder()
                    .responseModalities(Arrays.asList("TEXT", "IMAGE"))
                    .build();

            // 이미지 생성 요청
            GenerateContentResponse response = client.models.generateContent(
                    IMAGE_MODEL,
                    prompt,
                    config
            );

            // 응답에서 이미지 추출
            if (response.parts() != null) {
                for (Part part : response.parts()) {
                    if (part.inlineData().isPresent()) {
                        var inlineData = part.inlineData().get();

                        if (inlineData.mimeType().isPresent() &&
                                inlineData.mimeType().get().startsWith("image/") &&
                                inlineData.data().isPresent()) {

                            byte[] imageBytes = inlineData.data().get();

                            // 파일 확장자 결정
                            String mimeType = inlineData.mimeType().get();
                            String extension = mimeType.contains("png") ? ".png" :
                                    mimeType.contains("jpeg") ? ".jpg" : ".png";
                            String finalFilename = filename.replace(".png", extension);

                            Path filePath = Paths.get(directory, finalFilename);
                            Files.write(filePath, imageBytes);

                            log.info("[이미지생성] 이미지 저장 완료: {} ({}bytes, {})",
                                    filePath, imageBytes.length, mimeType);
                            return filePath.toString();
                        }
                    }
                }
            }

            // 텍스트 응답만 있는 경우 로그
            if (response.text() != null) {
                log.warn("[이미지생성] 텍스트만 응답됨: {}",
                        response.text().substring(0, Math.min(200, response.text().length())));
            }

            log.warn("[이미지생성] API 응답에서 이미지를 찾을 수 없음");
            return null;

        } catch (Exception e) {
            log.error("[이미지생성] 이미지 생성 실패: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 이미지 생성 프롬프트 구성
     */
    private String buildImagePrompt(String productName, String categoryName, String imageType) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Create a professional e-commerce product image. ");
        prompt.append("Product: ").append(productName).append(". ");
        prompt.append("Category: ").append(categoryName).append(". ");

        switch (imageType) {
            case "thumbnail":
                prompt.append("Style: Clean white background, centered product, high quality, professional lighting. ");
                prompt.append("This is the main thumbnail image for online shopping. ");
                break;
            case "detail1":
                prompt.append("Style: Show the product from a different angle, detailed view. ");
                break;
            case "detail2":
                prompt.append("Style: Close-up shot showing product details and texture. ");
                break;
            case "detail3":
                prompt.append("Style: Lifestyle image showing the product in use or context. ");
                break;
        }

        prompt.append("Resolution: 512x512 pixels. No text or watermarks.");

        return prompt.toString();
    }

    /**
     * 플레이스홀더 이미지 생성 (API 비활성화 시 또는 실패 시)
     */
    private boolean generatePlaceholderImages(Product product) {
        try {
            String productDir = uploadPath + "/products/" + product.getProductId();
            Files.createDirectories(Paths.get(productDir));

            // 플레이스홀더 썸네일 생성
            String thumbnailPath = createPlaceholderImage(
                    productDir, "thumbnail.png", product.getProductName(), "#6B7280");

            if (thumbnailPath != null) {
                product.setThumbnailUrl("/uploads/products/" + product.getProductId() + "/thumbnail.png");
                productRepository.save(product);
            }

            // 플레이스홀더 상세 이미지 3장
            String[] colors = {"#4B5563", "#374151", "#1F2937"};
            for (int i = 1; i <= 3; i++) {
                String detailPath = createPlaceholderImage(
                        productDir, "detail_" + i + ".png", product.getProductName() + " #" + i, colors[i-1]);

                if (detailPath != null) {
                    ProductImage productImage = ProductImage.builder()
                            .product(product)
                            .imageUrl("/uploads/products/" + product.getProductId() + "/detail_" + i + ".png")
                            .imageOrder(i)
                            .build();
                    productImageRepository.save(productImage);
                }
            }

            log.info("[이미지생성] 상품 {} 플레이스홀더 이미지 생성 완료", product.getProductId());
            return true;

        } catch (Exception e) {
            log.error("[이미지생성] 플레이스홀더 생성 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 플레이스홀더 PNG 이미지 생성
     */
    private String createPlaceholderImage(String directory, String filename, String text, String hexColor) {
        try {
            // 512x512 PNG 이미지 생성 (Java 2D Graphics 사용)
            int width = 512;
            int height = 512;

            java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
                    width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);

            java.awt.Graphics2D g2d = image.createGraphics();

            // 배경색 설정
            java.awt.Color bgColor = java.awt.Color.decode(hexColor);
            g2d.setColor(bgColor);
            g2d.fillRect(0, 0, width, height);

            // 텍스트 설정
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 24));

            java.awt.FontMetrics fm = g2d.getFontMetrics();

            // 텍스트가 너무 길면 줄바꿈
            String displayText = text.length() > 20 ? text.substring(0, 17) + "..." : text;
            int textWidth = fm.stringWidth(displayText);
            int x = (width - textWidth) / 2;
            int y = height / 2;

            g2d.drawString(displayText, x, y);

            // "상품 이미지" 추가
            g2d.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 16));
            String subText = "Product Image";
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(subText);
            x = (width - textWidth) / 2;
            g2d.drawString(subText, x, y + 30);

            g2d.dispose();

            // 파일 저장
            Path filePath = Paths.get(directory, filename);
            javax.imageio.ImageIO.write(image, "PNG", filePath.toFile());

            return filePath.toString();

        } catch (Exception e) {
            log.error("[이미지생성] 플레이스홀더 이미지 생성 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 일일 카운터 리셋 체크
     */
    private void resetDailyCounterIfNeeded() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastResetDate)) {
            dailyGeneratedCount = 0;
            lastResetDate = today;
            log.info("[이미지생성] 일일 카운터 리셋됨");
        }
    }

    /**
     * 현재 일일 사용량 조회
     */
    public Map<String, Object> getDailyUsageStatus() {
        resetDailyCounterIfNeeded();

        Map<String, Object> status = new HashMap<>();
        status.put("dailyUsed", dailyGeneratedCount);
        status.put("dailyLimit", dailyLimit);
        status.put("remainingQuota", dailyLimit - dailyGeneratedCount);
        status.put("generationEnabled", generationEnabled);
        status.put("hasApiKey", !geminiApiKey.isEmpty());
        status.put("lastResetDate", lastResetDate.toString());

        return status;
    }

    /**
     * 일일 카운터 강제 리셋 (관리자용)
     */
    public Map<String, Object> forceResetDailyCounter() {
        int previousCount = dailyGeneratedCount;
        dailyGeneratedCount = 0;
        lastResetDate = LocalDate.now();

        log.info("[이미지생성] 일일 카운터 강제 리셋: {} -> 0", previousCount);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "일일 카운터가 리셋되었습니다. (" + previousCount + " -> 0)");
        result.put("previousCount", previousCount);
        result.put("dailyUsed", dailyGeneratedCount);
        result.put("dailyLimit", dailyLimit);
        result.put("remainingQuota", dailyLimit - dailyGeneratedCount);

        return result;
    }

    /**
     * 이미지 없는 상품 수 조회
     */
    public int countProductsWithoutImages() {
        return productRepository.countProductsWithoutImages();
    }
}