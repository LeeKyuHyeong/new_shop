package com.kh.shop.scheduler;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.ProductTemplate;
import com.kh.shop.repository.CategoryRepository;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.ProductTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductBatchScheduler {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductTemplateRepository templateRepository;
    private final Random random = new Random();

    // ==================== 공통 데이터 (DB에 없을 때 기본값) ====================
    private final String[] DEFAULT_ADJECTIVES = {
            "프리미엄", "베이직", "클래식", "모던", "빈티지", "캐주얼",
            "트렌디", "시그니처", "에센셜", "럭셔리", "데일리", "심플"
    };

    private final String[] DEFAULT_COLORS = {
            "블랙", "화이트", "네이비", "그레이", "베이지", "카키", "브라운",
            "아이보리", "차콜", "버건디", "올리브", "크림"
    };

    private final String[] CLOTHES_SIZES = {"S", "M", "L", "XL", "FREE"};
    private final int[] SHOES_SIZES = {230, 235, 240, 245, 250, 255, 260, 265, 270, 275, 280};

    private final String[] DEFAULT_ITEMS = {"상품"};
    private final String[] DEFAULT_STYLES = {"스탠다드"};
    private final String[] DEFAULT_MATERIALS = {"혼방"};
    private final String DEFAULT_DESCRIPTION = "좋은 품질의 %s입니다. 다양한 스타일링이 가능합니다.";

    /**
     * 매시 10분에 랜덤 상품 1개 등록
     */
    @Scheduled(cron = "0 10 * * * *")
    @Transactional
    public void createRandomProduct() {
        log.info("========== [배치] 랜덤 상품 등록 시작 ==========");

        try {
            // 활성화된 하위 카테고리 조회
            List<Category> categories = categoryRepository.findByUseYnOrderByCategoryOrder("Y");
            List<Category> childCategories = categories.stream()
                    .filter(c -> c.getParent() != null)
                    .toList();

            if (childCategories.isEmpty()) {
                log.warn("[배치] 등록 가능한 카테고리가 없습니다.");
                return;
            }

            // 랜덤 카테고리 선택
            Category selectedCategory = childCategories.get(random.nextInt(childCategories.size()));
            Integer categoryId = selectedCategory.getCategoryId();

            // 카테고리별 템플릿 조회
            List<String> items = getTemplateValues(categoryId, "ITEM");
            List<String> styles = getTemplateValues(categoryId, "STYLE");
            List<String> materials = getTemplateValues(categoryId, "MATERIAL");
            List<String> descriptions = getTemplateValues(categoryId, "DESCRIPTION");

            // 상품 정보 생성
            String productName = generateProductName(items, styles, materials);
            int price = generatePrice(selectedCategory);
            int discount = generateDiscount();
            int stock = generateStock();
            String color = DEFAULT_COLORS[random.nextInt(DEFAULT_COLORS.length)];
            String size = generateSize(selectedCategory.getSizeType());
            String description = generateDescription(productName, descriptions);

            // 상품 생성
            Product product = Product.builder()
                    .category(selectedCategory)
                    .productName(productName)
                    .productPrice(price)
                    .productDiscount(discount)
                    .productStock(stock)
                    .color(color)
                    .size(size)
                    .productDescription(description)
                    .thumbnailUrl("/images/default-product.png")
                    .build();

            Product saved = productRepository.save(product);

            log.info("[배치] 상품 등록 완료 - ID: {}, 이름: {}, 카테고리: {}, 가격: {}원",
                    saved.getProductId(),
                    saved.getProductName(),
                    selectedCategory.getCategoryName(),
                    saved.getProductPrice());

        } catch (Exception e) {
            log.error("[배치] 상품 등록 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 랜덤 상품 등록 종료 ==========");
    }

    /**
     * 카테고리별 템플릿 값 조회 (없으면 기본값 반환)
     */
    private List<String> getTemplateValues(Integer categoryId, String templateType) {
        List<ProductTemplate> templates = templateRepository
                .findByCategoryCategoryIdAndTemplateTypeAndUseYn(categoryId, templateType, "Y");

        if (!templates.isEmpty()) {
            return templates.stream()
                    .map(ProductTemplate::getTemplateValue)
                    .toList();
        }

        // 상위 카테고리에서 조회 시도
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null && category.getParent() != null) {
            templates = templateRepository.findByCategoryCategoryIdAndTemplateTypeAndUseYn(
                    category.getParent().getCategoryId(), templateType, "Y");

            if (!templates.isEmpty()) {
                return templates.stream()
                        .map(ProductTemplate::getTemplateValue)
                        .toList();
            }
        }

        // 기본값 반환
        return switch (templateType) {
            case "ITEM" -> List.of(DEFAULT_ITEMS);
            case "STYLE" -> List.of(DEFAULT_STYLES);
            case "MATERIAL" -> List.of(DEFAULT_MATERIALS);
            case "DESCRIPTION" -> List.of(DEFAULT_DESCRIPTION);
            default -> List.of();
        };
    }

    /**
     * 상품명 생성
     */
    private String generateProductName(List<String> items, List<String> styles, List<String> materials) {
        String adj = DEFAULT_ADJECTIVES[random.nextInt(DEFAULT_ADJECTIVES.length)];
        String item = items.get(random.nextInt(items.size()));
        String style = styles.get(random.nextInt(styles.size()));
        String material = materials.get(random.nextInt(materials.size()));

        // 상품명 조합 패턴 (랜덤 선택)
        int pattern = random.nextInt(4);
        return switch (pattern) {
            case 0 -> String.format("%s %s %s %s", adj, material, style, item);
            case 1 -> String.format("%s %s %s", adj, material, item);
            case 2 -> String.format("%s %s %s", adj, style, item);
            default -> String.format("%s %s", adj, item);
        };
    }

    /**
     * 가격 생성 (카테고리 설정 기반)
     */
    private int generatePrice(Category category) {
        int minPrice = category.getMinPrice() != null ? category.getMinPrice() : 19000;
        int maxPrice = category.getMaxPrice() != null ? category.getMaxPrice() : 99000;

        // 1000원 단위로 생성
        int range = (maxPrice - minPrice) / 1000;
        if (range <= 0) range = 1;
        return minPrice + (random.nextInt(range + 1) * 1000);
    }

    /**
     * 사이즈 생성 (카테고리 sizeType 기반)
     */
    private String generateSize(String sizeType) {
        if (sizeType == null) sizeType = "CLOTHES";

        return switch (sizeType.toUpperCase()) {
            case "SHOES" -> String.valueOf(SHOES_SIZES[random.nextInt(SHOES_SIZES.length)]);
            case "FREE" -> "FREE";
            default -> CLOTHES_SIZES[random.nextInt(CLOTHES_SIZES.length)];
        };
    }

    /**
     * 할인율 생성
     */
    private int generateDiscount() {
        int[] discounts = {0, 0, 0, 10, 10, 20, 20, 30, 40, 50};
        return discounts[random.nextInt(discounts.length)];
    }

    /**
     * 재고 생성
     */
    private int generateStock() {
        return 10 + random.nextInt(191);
    }

    /**
     * 설명 생성
     */
    private String generateDescription(String productName, List<String> descriptions) {
        String template = descriptions.get(random.nextInt(descriptions.size()));
        return String.format(template, productName);
    }
}