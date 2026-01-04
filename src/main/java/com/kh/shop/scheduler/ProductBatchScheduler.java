package com.kh.shop.scheduler;

import com.kh.shop.entity.Category;
import com.kh.shop.entity.Product;
import com.kh.shop.repository.CategoryRepository;
import com.kh.shop.repository.ProductRepository;
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
    private final Random random = new Random();

    // 상품명 조합용 데이터
    private final String[] ADJECTIVES = {
            "프리미엄", "베이직", "클래식", "모던", "빈티지", "캐주얼", "포멀",
            "트렌디", "시그니처", "에센셜", "럭셔리", "데일리", "심플", "엘레강스"
    };

    private final String[] STYLES = {
            "오버핏", "슬림핏", "레귤러핏", "루즈핏", "크롭", "롱", "미디", "하이웨이스트"
    };

    private final String[] ITEMS = {
            "티셔츠", "셔츠", "블라우스", "니트", "가디건", "자켓", "코트",
            "청바지", "슬랙스", "스커트", "원피스", "후드티", "맨투맨", "조거팬츠",
            "트렌치코트", "패딩", "점퍼", "베스트", "반바지", "레깅스"
    };

    private final String[] MATERIALS = {
            "코튼", "린넨", "데님", "울", "캐시미어", "폴리", "실크", "트위드", "벨벳", "플리스"
    };

    private final String[] COLORS = {
            "블랙", "화이트", "네이비", "그레이", "베이지", "카키", "브라운",
            "아이보리", "차콜", "버건디", "올리브", "크림"
    };

    private final String[] SIZES = {
            "S", "M", "L", "XL", "FREE"
    };

    /**
     * 매시 10분에 랜덤 상품 1개 등록
     * cron = "초 분 시 일 월 요일"
     */
    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void createRandomProduct() {
        log.info("========== [배치] 랜덤 상품 등록 시작 ==========");

        try {
            // 활성화된 카테고리 조회 (하위 카테고리만)
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

            // 랜덤 상품 정보 생성
            String productName = generateProductName();
            int price = generatePrice();
            int discount = generateDiscount();
            int stock = generateStock();
            String color = COLORS[random.nextInt(COLORS.length)];
            String size = SIZES[random.nextInt(SIZES.length)];
            String description = generateDescription(productName);

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
                    .thumbnailUrl("/images/default-product.png")  // 기본 이미지
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
     * 랜덤 상품명 생성
     */
    private String generateProductName() {
        String adj = ADJECTIVES[random.nextInt(ADJECTIVES.length)];
        String material = MATERIALS[random.nextInt(MATERIALS.length)];
        String style = STYLES[random.nextInt(STYLES.length)];
        String item = ITEMS[random.nextInt(ITEMS.length)];

        // 50% 확률로 스타일 포함
        if (random.nextBoolean()) {
            return String.format("%s %s %s %s", adj, material, style, item);
        } else {
            return String.format("%s %s %s", adj, material, item);
        }
    }

    /**
     * 랜덤 가격 생성 (19,000 ~ 299,000원, 1000원 단위)
     */
    private int generatePrice() {
        int base = 19 + random.nextInt(281);  // 19 ~ 299
        return base * 1000;
    }

    /**
     * 랜덤 할인율 생성 (0, 10, 20, 30, 40, 50%)
     */
    private int generateDiscount() {
        int[] discounts = {0, 0, 0, 10, 10, 20, 20, 30, 40, 50};
        return discounts[random.nextInt(discounts.length)];
    }

    /**
     * 랜덤 재고 생성 (10 ~ 200개)
     */
    private int generateStock() {
        return 10 + random.nextInt(191);
    }

    /**
     * 상품 설명 생성
     */
    private String generateDescription(String productName) {
        String[] templates = {
                "트렌디한 디자인의 %s입니다. 다양한 스타일링이 가능합니다.",
                "편안한 착용감의 %s입니다. 데일리 아이템으로 추천드립니다.",
                "고급스러운 소재로 제작된 %s입니다. 어떤 룩에도 잘 어울립니다.",
                "실용적이면서 세련된 %s입니다. 다양한 컬러로 만나보세요.",
                "세련된 실루엣의 %s입니다. 특별한 날에도 좋습니다."
        };
        String template = templates[random.nextInt(templates.length)];
        return String.format(template, productName);
    }
}