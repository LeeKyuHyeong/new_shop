package com.kh.shop.scheduler;

import com.kh.shop.entity.Order;
import com.kh.shop.entity.OrderItem;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import com.kh.shop.repository.OrderRepository;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreateBatchScheduler {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final Random random = new Random();

    // 배송지 데이터
    private final String[][] ADDRESSES = {
            {"서울특별시 강남구 테헤란로 123", "삼성동 OO빌딩 5층"},
            {"서울특별시 서초구 서초대로 456", "서초동 OO타워 10층"},
            {"서울특별시 마포구 홍대입구역 789", "서교동 OO오피스텔 302호"},
            {"경기도 성남시 분당구 판교로 111", "판교 OO프라자 8층"},
            {"경기도 수원시 영통구 광교로 222", "광교 OO아파트 101동 1001호"},
            {"부산광역시 해운대구 해운대로 333", "우동 OO마린시티 2305호"},
            {"인천광역시 연수구 송도과학로 444", "송도 OO센트럴파크 1502호"},
            {"대전광역시 유성구 대학로 555", "봉명동 OO프라자 3층"},
            {"대구광역시 수성구 범어로 666", "범어동 OO타워 12층"},
            {"광주광역시 서구 상무중앙로 777", "치평동 OO빌딩 7층"}
    };

    private final String[] POSTAL_CODES = {
            "06234", "06621", "04038", "13487", "16514",
            "48099", "21990", "34126", "42179", "61945"
    };

    private final String[] MEMOS = {
            "부재시 문 앞에 놓아주세요",
            "배송 전 연락 부탁드립니다",
            "경비실에 맡겨주세요",
            "문 앞 택배함에 넣어주세요",
            "조심히 다뤄주세요",
            "",
            "",
            ""
    };

    private final String[] PAYMENT_METHODS = {"CARD", "CARD", "CARD", "KAKAO", "NAVER"};

    /**
     * 매시 15분에 랜덤 주문 1건 생성
     */
    @Scheduled(cron = "0 15 * * * *")
    @Transactional
    public void createRandomOrder() {
        log.info("========== [배치] 랜덤 주문 생성 시작 ==========");

        try {
            // 1. 활성 사용자 조회 (USER 권한만)
            List<User> users = userRepository.findByUserRoleOrderByCreatedDateDesc("USER");
            if (users.isEmpty()) {
                log.warn("[배치] 주문 가능한 사용자가 없습니다.");
                return;
            }

            // 2. 재고 있는 상품 조회
            List<Product> products = productRepository.findByUseYnOrderByProductOrderAsc("Y");
            List<Product> availableProducts = products.stream()
                    .filter(p -> p.getProductStock() != null && p.getProductStock() > 0)
                    .toList();

            if (availableProducts.isEmpty()) {
                log.warn("[배치] 주문 가능한 상품이 없습니다.");
                return;
            }

            // 3. 랜덤 사용자 선택
            User selectedUser = users.get(random.nextInt(users.size()));

            // 4. 랜덤 상품 선택 (1~3개)
            int itemCount = 1 + random.nextInt(3);
            Set<Product> selectedProducts = new HashSet<>();
            while (selectedProducts.size() < itemCount && selectedProducts.size() < availableProducts.size()) {
                selectedProducts.add(availableProducts.get(random.nextInt(availableProducts.size())));
            }

            // 5. 주문번호 생성
            String orderNumber = generateOrderNumber();

            // 6. 배송 정보 생성
            int addrIndex = random.nextInt(ADDRESSES.length);
            String receiverName = selectedUser.getUserName();
            String receiverPhone = generatePhoneNumber();
            String receiverAddress = ADDRESSES[addrIndex][0];
            String receiverAddressDetail = ADDRESSES[addrIndex][1];
            String postalCode = POSTAL_CODES[addrIndex];
            String orderMemo = MEMOS[random.nextInt(MEMOS.length)];
            String paymentMethod = PAYMENT_METHODS[random.nextInt(PAYMENT_METHODS.length)];

            // 7. 주문 생성
            Order order = Order.builder()
                    .user(selectedUser)
                    .orderNumber(orderNumber)
                    .receiverName(receiverName)
                    .receiverPhone(receiverPhone)
                    .receiverAddress(receiverAddress)
                    .receiverAddressDetail(receiverAddressDetail)
                    .postalCode(postalCode)
                    .orderMemo(orderMemo)
                    .paymentMethod(paymentMethod)
                    .totalPrice(0)
                    .discountAmount(0)
                    .deliveryFee(0)
                    .finalPrice(0)
                    .orderStatus("PAID")  // 결제완료 상태로 생성
                    .paymentStatus("COMPLETED")
                    .paidAt(LocalDateTime.now())
                    .build();

            // 8. 주문 상품 생성 및 금액 계산
            int totalPrice = 0;
            int discountAmount = 0;
            List<OrderItem> orderItems = new ArrayList<>();

            for (Product product : selectedProducts) {
                int quantity = 1 + random.nextInt(3);  // 1~3개
                int itemPrice = product.getDiscountedPrice();
                int originalPrice = product.getProductPrice();
                int itemTotal = itemPrice * quantity;
                int itemDiscount = (originalPrice - itemPrice) * quantity;

                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(product)
                        .productName(product.getProductName())
                        .productPrice(originalPrice)
                        .productDiscount(product.getProductDiscount())
                        .quantity(quantity)
                        .itemPrice(itemPrice)
                        .totalPrice(itemTotal)
                        .thumbnailUrl(product.getThumbnailUrl())
                        .color(product.getColor())
                        .size(product.getSize())
                        .build();

                orderItems.add(orderItem);
                totalPrice += originalPrice * quantity;
                discountAmount += itemDiscount;

                // 재고 차감
                product.setProductStock(product.getProductStock() - quantity);
            }

            // 9. 배송비 계산 (3만원 이상 무료)
            int finalPrice = totalPrice - discountAmount;
            int deliveryFee = finalPrice >= 30000 ? 0 : 3000;
            finalPrice += deliveryFee;

            // 10. 주문 금액 설정
            order.setTotalPrice(totalPrice);
            order.setDiscountAmount(discountAmount);
            order.setDeliveryFee(deliveryFee);
            order.setFinalPrice(finalPrice);
            order.setOrderItems(orderItems);

            // 11. 저장
            Order savedOrder = orderRepository.save(order);

            log.info("[배치] 주문 생성 완료 - 주문번호: {}, 사용자: {}, 상품수: {}, 결제금액: {}원",
                    savedOrder.getOrderNumber(),
                    selectedUser.getUserId(),
                    orderItems.size(),
                    finalPrice);

        } catch (Exception e) {
            log.error("[배치] 주문 생성 실패: {}", e.getMessage(), e);
        }

        log.info("========== [배치] 랜덤 주문 생성 종료 ==========");
    }

    /**
     * 주문번호 생성 (ORD + yyyyMMddHHmmss + 랜덤4자리)
     */
    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int randomNum = 1000 + random.nextInt(9000);
        return "ORD" + timestamp + randomNum;
    }

    /**
     * 랜덤 전화번호 생성
     */
    private String generatePhoneNumber() {
        String[] middleNums = {"1234", "5678", "9012", "3456", "7890", "2345", "6789", "0123"};
        String[] lastNums = {"1111", "2222", "3333", "4444", "5555", "6666", "7777", "8888", "9999"};
        return "010-" + middleNums[random.nextInt(middleNums.length)] + "-" + lastNums[random.nextInt(lastNums.length)];
    }
}