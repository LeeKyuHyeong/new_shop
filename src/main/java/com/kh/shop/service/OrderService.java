package com.kh.shop.service;

import com.kh.shop.entity.*;
import com.kh.shop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    // 전체 주문 목록 조회
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findByUseYnOrderByCreatedDateDesc("Y");
    }

    // 사용자별 주문 목록 조회
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return orderRepository.findByUserAndUseYnOrderByCreatedDateDesc(user, "Y");
    }

    // 주문 상세 조회
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findByIdWithItems(orderId);
    }

    // 주문번호로 조회
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumberAndUseYn(orderNumber, "Y");
    }

    // 상태별 주문 목록
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByOrderStatusAndUseYnOrderByCreatedDateDesc(status, "Y");
    }

    // 주문 생성 (장바구니에서)
    @Transactional
    public Order createOrderFromCart(String userId, List<Long> cartIds,
                                     String receiverName, String receiverPhone,
                                     String postalCode, String receiverAddress,
                                     String receiverAddressDetail, String orderMemo,
                                     String paymentMethod) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Cart> cartItems = cartRepository.findByUserAndCartIdIn(user, cartIds);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("장바구니에 상품이 없습니다.");
        }

        // 총 금액 계산
        int totalPrice = 0;
        int discountAmount = 0;

        for (Cart cart : cartItems) {
            Product product = cart.getProduct();
            if (product.getProductStock() < cart.getQuantity()) {
                throw new RuntimeException(product.getProductName() + " 상품의 재고가 부족합니다.");
            }
            totalPrice += product.getProductPrice() * cart.getQuantity();
            discountAmount += (product.getProductPrice() - product.getDiscountedPrice()) * cart.getQuantity();
        }

        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;  // 5만원 이상 무료배송
        int finalPrice = totalPrice - discountAmount + deliveryFee;

        // 주문 생성
        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .postalCode(postalCode)
                .receiverAddress(receiverAddress)
                .receiverAddressDetail(receiverAddressDetail)
                .orderMemo(orderMemo)
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .deliveryFee(deliveryFee)
                .finalPrice(finalPrice)
                .paymentMethod(paymentMethod)
                .build();

        order = orderRepository.save(order);

        // 주문 상품 생성 및 재고 차감
        for (Cart cart : cartItems) {
            Product product = cart.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .productName(product.getProductName())
                    .productPrice(product.getProductPrice())
                    .productDiscount(product.getProductDiscount())
                    .quantity(cart.getQuantity())
                    .itemPrice(product.getDiscountedPrice())
                    .totalPrice(product.getDiscountedPrice() * cart.getQuantity())
                    .thumbnailUrl(product.getThumbnailUrl())
                    .color(product.getColor())
                    .size(product.getSize())
                    .build();

            orderItemRepository.save(orderItem);

            // 재고 차감
            product.setProductStock(product.getProductStock() - cart.getQuantity());
            productRepository.save(product);

            // 장바구니 삭제
            cart.setUseYn("N");
            cartRepository.save(cart);
        }

        return order;
    }

    // 바로 구매
    @Transactional
    public Order createDirectOrder(String userId, Long productId, Integer quantity,
                                   String receiverName, String receiverPhone,
                                   String postalCode, String receiverAddress,
                                   String receiverAddressDetail, String orderMemo,
                                   String paymentMethod) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId).orElseThrow(() ->
                new RuntimeException("상품을 찾을 수 없습니다."));

        if (product.getProductStock() < quantity) {
            throw new RuntimeException("상품의 재고가 부족합니다.");
        }

        int totalPrice = product.getProductPrice() * quantity;
        int discountAmount = (product.getProductPrice() - product.getDiscountedPrice()) * quantity;
        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;
        int finalPrice = totalPrice - discountAmount + deliveryFee;

        Order order = Order.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .receiverName(receiverName)
                .receiverPhone(receiverPhone)
                .postalCode(postalCode)
                .receiverAddress(receiverAddress)
                .receiverAddressDetail(receiverAddressDetail)
                .orderMemo(orderMemo)
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .deliveryFee(deliveryFee)
                .finalPrice(finalPrice)
                .paymentMethod(paymentMethod)
                .build();

        order = orderRepository.save(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getProductName())
                .productPrice(product.getProductPrice())
                .productDiscount(product.getProductDiscount())
                .quantity(quantity)
                .itemPrice(product.getDiscountedPrice())
                .totalPrice(product.getDiscountedPrice() * quantity)
                .thumbnailUrl(product.getThumbnailUrl())
                .color(product.getColor())
                .size(product.getSize())
                .build();

        orderItemRepository.save(orderItem);

        // 재고 차감
        product.setProductStock(product.getProductStock() - quantity);
        productRepository.save(product);

        return order;
    }

    // 주문 상태 변경
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new RuntimeException("주문을 찾을 수 없습니다."));

        order.setOrderStatus(status);

        switch (status) {
            case "PAID":
                order.setPaymentStatus("COMPLETED");
                order.setPaidAt(LocalDateTime.now());
                break;
            case "SHIPPING":
                order.setShippedAt(LocalDateTime.now());
                break;
            case "DELIVERED":
                order.setDeliveredAt(LocalDateTime.now());
                break;
            case "CANCELLED":
                order.setCancelledAt(LocalDateTime.now());
                order.setPaymentStatus("CANCELLED");
                // 재고 복구
                restoreStock(order);
                break;
        }

        return orderRepository.save(order);
    }

    // 주문 취소
    @Transactional
    public Order cancelOrder(Long orderId, String cancelReason) {
        Order order = orderRepository.findByIdWithItems(orderId).orElseThrow(() ->
                new RuntimeException("주문을 찾을 수 없습니다."));

        if ("DELIVERED".equals(order.getOrderStatus())) {
            throw new RuntimeException("배송 완료된 주문은 취소할 수 없습니다.");
        }

        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("CANCELLED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(cancelReason);

        // 재고 복구
        restoreStock(order);

        return orderRepository.save(order);
    }

    // 재고 복구
    private void restoreStock(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setProductStock(product.getProductStock() + item.getQuantity());
            productRepository.save(product);
        }
    }

    // 주문 삭제 (soft delete)
    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new RuntimeException("주문을 찾을 수 없습니다."));
        order.setUseYn("N");
        orderRepository.save(order);
    }

    // 주문번호 생성
    private String generateOrderNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.countTodayOrders() + 1;
        return date + String.format("%05d", count);
    }
}