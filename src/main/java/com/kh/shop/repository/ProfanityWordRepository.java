package com.kh.shop.repository;

import com.kh.shop.entity.ProfanityWord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfanityWordRepository extends JpaRepository<ProfanityWord, Long> {

    // 활성화된 비속어 목록
    List<ProfanityWord> findByIsActiveTrue();

    // 단어로 조회
    Optional<ProfanityWord> findByWord(String word);

    // 단어 존재 여부
    boolean existsByWord(String word);

    // 카테고리별 조회
    List<ProfanityWord> findByCategoryAndIsActiveTrue(String category);

    // 검색 (단어 또는 설명)
    @Query("SELECT p FROM ProfanityWord p WHERE " +
            "(LOWER(p.word) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<ProfanityWord> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 카테고리별 검색
    @Query("SELECT p FROM ProfanityWord p WHERE " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:keyword IS NULL OR LOWER(p.word) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<ProfanityWord> findByFilter(@Param("category") String category,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    // 전체 목록 (페이징)
    Page<ProfanityWord> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 카테고리별 개수
    @Query("SELECT p.category, COUNT(p) FROM ProfanityWord p WHERE p.isActive = true GROUP BY p.category")
    List<Object[]> countByCategory();

    // 시스템 기본 비속어 개수
    long countByIsSystemTrue();

    // 사용자 추가 비속어 개수
    long countByIsSystemFalse();

    // 활성화된 비속어 개수
    long countByIsActiveTrue();
}