package com.kh.shop.repository;

import com.kh.shop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    // 상품별 이미지 조회
    List<ProductImage> findByProductProductIdAndUseYnOrderByImageOrderAsc(Long productId, String useYn);

    // 상품별 이미지 전체 삭제용
    List<ProductImage> findByProductProductId(Long productId);
}