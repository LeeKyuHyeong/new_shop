package com.kh.shop.service;

import com.kh.shop.entity.Product;
import com.kh.shop.entity.User;
import com.kh.shop.entity.Wishlist;
import com.kh.shop.repository.ProductRepository;
import com.kh.shop.repository.UserRepository;
import com.kh.shop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * 위시리스트에 상품 추가
     */
    @Transactional
    public boolean addToWishlist(String userId, Long productId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Product product = productOpt.get();

        // 이미 위시리스트에 있는지 확인
        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            return false;
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
        log.info("[위시리스트] 상품 추가 - 사용자: {}, 상품ID: {}", userId, productId);
        return true;
    }

    /**
     * 위시리스트에서 상품 제거
     */
    @Transactional
    public boolean removeFromWishlist(String userId, Long productId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Product product = productOpt.get();

        wishlistRepository.deleteByUserAndProduct(user, product);
        log.info("[위시리스트] 상품 제거 - 사용자: {}, 상품ID: {}", userId, productId);
        return true;
    }

    /**
     * 위시리스트 토글 (있으면 제거, 없으면 추가)
     */
    @Transactional
    public boolean toggleWishlist(String userId, Long productId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        Product product = productOpt.get();

        if (wishlistRepository.existsByUserAndProduct(user, product)) {
            wishlistRepository.deleteByUserAndProduct(user, product);
            log.info("[위시리스트] 상품 제거 (토글) - 사용자: {}, 상품ID: {}", userId, productId);
            return false; // 제거됨
        } else {
            Wishlist wishlist = Wishlist.builder()
                    .user(user)
                    .product(product)
                    .build();
            wishlistRepository.save(wishlist);
            log.info("[위시리스트] 상품 추가 (토글) - 사용자: {}, 상품ID: {}", userId, productId);
            return true; // 추가됨
        }
    }

    /**
     * 사용자의 위시리스트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByUser(String userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return List.of();
        }
        return wishlistRepository.findByUserWithProduct(userOpt.get());
    }

    /**
     * 위시리스트 상품 개수 조회
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(String userId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return 0;
        }
        return wishlistRepository.countByUser(userOpt.get());
    }

    /**
     * 특정 상품이 위시리스트에 있는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(String userId, Long productId) {
        Optional<User> userOpt = userRepository.findByUserId(userId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (userOpt.isEmpty() || productOpt.isEmpty()) {
            return false;
        }

        return wishlistRepository.existsByUserAndProduct(userOpt.get(), productOpt.get());
    }

    /**
     * 특정 상품의 찜 개수 조회
     */
    @Transactional(readOnly = true)
    public long getProductWishCount(Long productId) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            return 0;
        }
        return wishlistRepository.countByProduct(productOpt.get());
    }

    /**
     * 위시리스트 ID로 삭제
     */
    @Transactional
    public boolean removeById(Long wishlistId, String userId) {
        Optional<Wishlist> wishlistOpt = wishlistRepository.findById(wishlistId);
        if (wishlistOpt.isEmpty()) {
            return false;
        }

        Wishlist wishlist = wishlistOpt.get();
        // 본인 위시리스트인지 확인
        if (!wishlist.getUser().getUserId().equals(userId)) {
            return false;
        }

        wishlistRepository.delete(wishlist);
        return true;
    }
}