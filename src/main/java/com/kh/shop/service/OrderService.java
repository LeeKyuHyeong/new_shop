package com.kh.shop.service;

import com.kh.shop.entity.*;
import com.kh.shop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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

    // 사용자별 주문 목록 조회 (orderItems 까지 fetch -> N+1 방지)
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return orderRepository.findByUserAndUseYnWithItemsOrderByCreatedDateDesc(user, "Y");
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

    // Admin: 페이징 주문 목록 (전체) — 메모리 안전 버전
    @Transactional(readOnly = true)
    public Page<Order> getAllOrdersPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUseYnOrderByCreatedDateDesc("Y", pageable);
    }

    // Admin: 페이징 주문 목록 (상태별)
    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatusPaged(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByOrderStatusAndUseYnOrderByCreatedDateDesc(status, "Y", pageable);
    }

    // 주문 생성 (장바구니에서) - Cart의 옵션 사용
    // 재고 검증/차감을 PESSIMISTIC_WRITE 락 하에 수행하여 동시 주문 시 overselling 차단.
    // 여러 상품을 잠그므로 productId 오름차순으로 락 획득 -> 데드락 방지.
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

        // 데드락 방지: productId 오름차순 정렬 후 락 획득
        cartItems = cartItems.stream()
                .sorted(Comparator.comparing(c -> c.getProduct().getProductId()))
                .toList();

        // 락 잡은 Product 인스턴스를 cart 순서대로 보관 (이후 OrderItem 생성/재고 차감에 재사용)
        List<Product> lockedProducts = new ArrayList<>(cartItems.size());
        int totalPrice = 0;
        int discountAmount = 0;

        for (Cart cart : cartItems) {
            Long productId = cart.getProduct().getProductId();
            Product product = productRepository.findByIdForUpdate(productId).orElseThrow(() ->
                    new RuntimeException("상품을 찾을 수 없습니다."));
            if (product.getProductStock() < cart.getQuantity()) {
                throw new RuntimeException(product.getProductName() + " 상품의 재고가 부족합니다.");
            }
            lockedProducts.add(product);
            totalPrice += product.getProductPrice() * cart.getQuantity();
            discountAmount += (product.getProductPrice() - product.getDiscountedPrice()) * cart.getQuantity();
        }

        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;
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

        // 주문 상품 생성 및 재고 차감 - 위에서 락 잡은 Product 인스턴스 사용
        for (int i = 0; i < cartItems.size(); i++) {
            Cart cart = cartItems.get(i);
            Product product = lockedProducts.get(i);

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
                    .color(cart.getColor())
                    .size(cart.getSize())
                    .build();

            orderItemRepository.save(orderItem);

            // 재고 차감 (락 보유 중)
            product.setProductStock(product.getProductStock() - cart.getQuantity());
            productRepository.save(product);

            // 장바구니 삭제
            cart.setUseYn("N");
            cartRepository.save(cart);
        }

        return order;
    }

    // 바로 구매 - 옵션 파라미터 추가
    // 재고 검증/차감을 PESSIMISTIC_WRITE 락 하에 수행하여 동시 주문 시 overselling 차단.
    @Transactional
    public Order createDirectOrder(String userId, Long productId, Integer quantity,
                                   String color, String size,
                                   String receiverName, String receiverPhone,
                                   String postalCode, String receiverAddress,
                                   String receiverAddressDetail, String orderMemo,
                                   String paymentMethod) {

        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findByIdForUpdate(productId).orElseThrow(() ->
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

        // 빈 문자열 처리
        String normalizedColor = (color != null && !color.trim().isEmpty()) ? color.trim() : null;
        String normalizedSize = (size != null && !size.trim().isEmpty()) ? size.trim() : null;

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
                .color(normalizedColor)
                .size(normalizedSize)
                .build();

        orderItemRepository.save(orderItem);

        // 재고 차감
        product.setProductStock(product.getProductStock() - quantity);
        productRepository.save(product);

        return order;
    }

    // 결제 검증 통과 시 호출. Order PENDING -> PAID 전환.
    // PESSIMISTIC_WRITE 락으로 동시 verify 호출(예: 더블 클릭)이 모두 통과하는 것을 차단.
    @Transactional
    public Order markOrderPaid(Long orderId) {
        Order order = orderRepository.findByIdForUpdate(orderId).orElseThrow(() ->
                new RuntimeException("주문을 찾을 수 없습니다."));

        if (!"PENDING".equals(order.getOrderStatus()) || !"PENDING".equals(order.getPaymentStatus())) {
            throw new IllegalStateException("이미 처리된 주문입니다. (orderId=" + orderId
                    + ", orderStatus=" + order.getOrderStatus()
                    + ", paymentStatus=" + order.getPaymentStatus() + ")");
        }

        order.setOrderStatus("PAID");
        order.setPaymentStatus("COMPLETED");
        order.setPaidAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // 결제 검증 실패 시 호출. PENDING 상태일 때만 취소 + 재고 복구.
    // Order/Product 모두 락 잡고 idempotent 보장.
    @Transactional
    public Order cancelPendingOrder(Long orderId, String reason) {
        // 먼저 Order 락
        Order lockedOrder = orderRepository.findByIdForUpdate(orderId).orElseThrow(() ->
                new RuntimeException("주문을 찾을 수 없습니다."));

        if (!"PENDING".equals(lockedOrder.getOrderStatus())) {
            return lockedOrder;
        }

        // orderItems 페치를 위해 별도 조회 (락은 위에서 잡힌 상태)
        Order order = orderRepository.findByIdWithItems(orderId).orElse(lockedOrder);

        order.setOrderStatus("CANCELLED");
        order.setPaymentStatus("FAILED");
        order.setCancelledAt(LocalDateTime.now());
        order.setCancelReason(reason);
        restoreStock(order);
        return orderRepository.save(order);
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

        restoreStock(order);

        return orderRepository.save(order);
    }

    // 재고 복구
    // 데드락 방지 위해 productId 오름차순 정렬 후 락 획득.
    private void restoreStock(Order order) {
        List<OrderItem> sortedItems = order.getOrderItems().stream()
                .sorted(Comparator.comparing(i -> i.getProduct().getProductId()))
                .toList();

        for (OrderItem item : sortedItems) {
            Long productId = item.getProduct().getProductId();
            Product product = productRepository.findByIdForUpdate(productId).orElseThrow(() ->
                    new RuntimeException("재고 복구 대상 상품을 찾을 수 없습니다."));
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