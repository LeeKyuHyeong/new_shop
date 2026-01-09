package com.kh.shop.repository;

import com.kh.shop.entity.SearchKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchKeywordRepository extends JpaRepository<SearchKeyword, Long> {

    // 특정 날짜의 특정 키워드 조회
    Optional<SearchKeyword> findByKeywordAndSearchDate(String keyword, LocalDate searchDate);

    // 인기 검색어 조회 (최근 N일간 검색 횟수 합계 기준)
    @Query("SELECT sk.keyword, SUM(sk.searchCount) as totalCount FROM SearchKeyword sk WHERE sk.searchDate >= :startDate GROUP BY sk.keyword ORDER BY totalCount DESC")
    List<Object[]> findPopularKeywords(@Param("startDate") LocalDate startDate);

    // 특정 날짜의 인기 검색어
    List<SearchKeyword> findBySearchDateOrderBySearchCountDesc(LocalDate searchDate);

    // 오래된 검색어 데이터 삭제
    void deleteBySearchDateBefore(LocalDate date);
}