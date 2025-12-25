package com.kh.shop.service;

import com.kh.shop.entity.Cart;
import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import com.kh.shop.repository.CartRepository;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public List<Cart> getCartByUser(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return List.of();
        return cartRepository.findByUserWithProduct(user);
    }

    // 장바구니 추가
    @Transactional
    public Cart addToCart(String userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId).orElseThrow(() ->
                new RuntimeException("상품을 찾을 수 없습니다."));

        if (!"Y".equals(product.getUseYn())) {
            throw new RuntimeException("판매 중지된 상품입니다.");
        }

        if (product.getProductStock() < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        // 이미 장바구니에 있는 상품인지 확인
        Optional<Cart> existingCart = cartRepository.findByUserAndProductAndUseYn(user, product, "Y");

        if (existingCart.isPresent()) {
            // 수량 추가
            Cart cart = existingCart.get();
            int newQuantity = cart.getQuantity() + quantity;

            if (product.getProductStock() < newQuantity) {
                throw new RuntimeException("재고가 부족합니다. (현재 재고: " + product.getProductStock() + "개)");
            }

            cart.setQuantity(newQuantity);
            return cartRepository.save(cart);
        } else {
            // 새로 추가
            Cart cart = Cart.builder()
                    .user(user)
                    .product(product)
                    .quantity(quantity)
                    .build();
            return cartRepository.save(cart);
        }
    }

    // 장바구니 수량 변경
    @Transactional
    public Cart updateQuantity(Long cartId, Integer quantity, String userId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() ->
                new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        if (cart.getProduct().getProductStock() < quantity) {
            throw new RuntimeException("재고가 부족합니다.");
        }

        cart.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    // 장바구니 삭제
    @Transactional
    public void removeFromCart(Long cartId, String userId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() ->
                new RuntimeException("장바구니 항목을 찾을 수 없습니다."));

        if (!cart.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        cart.setUseYn("N");
        cartRepository.save(cart);
    }

    // 장바구니 선택 삭제
    @Transactional
    public void removeSelectedFromCart(List<Long> cartIds, String userId) {
        for (Long cartId : cartIds) {
            removeFromCart(cartId, userId);
        }
    }

    // 장바구니 전체 삭제
    @Transactional
    public void clearCart(String userId) {
        List<Cart> carts = getCartByUser(userId);
        for (Cart cart : carts) {
            cart.setUseYn("N");
            cartRepository.save(cart);
        }
    }

    // 장바구니 수량
    @Transactional(readOnly = true)
    public long getCartCount(String userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return 0;
        return cartRepository.countByUserAndUseYn(user, "Y");
    }

    // 장바구니 총 금액 계산
    @Transactional(readOnly = true)
    public int getCartTotalPrice(String userId) {
        List<Cart> carts = getCartByUser(userId);
        return carts.stream()
                .mapToInt(Cart::getTotalPrice)
                .sum();
    }
}