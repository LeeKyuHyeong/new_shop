package com.kh.shop.common.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PageResponseDTO<E> {

    private List<E> dtoList;

    private int totalPage;
    private int page;
    private int size;
    private long totalCount;

    private int start, end;
    private boolean prev, next;

    private List<Integer> pageList;

    // 검색 조건 유지용
    private String searchType;
    private String searchKeyword;
    private String sortField;
    private String sortDirection;
    private Integer categoryId;

    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(com.kh.shop.common.dto.PageRequestDTO pageRequestDTO, Page<E> result) {

        if (result == null || result.isEmpty()) {
            this.dtoList = List.of();
            this.totalPage = 0;
            this.page = pageRequestDTO.getPage();
            this.size = pageRequestDTO.getSize();
            this.totalCount = 0;
            this.start = 0;
            this.end = 0;
            this.prev = false;
            this.next = false;
            this.pageList = List.of();
        } else {
            this.dtoList = result.getContent();
            this.totalPage = result.getTotalPages();
            this.page = pageRequestDTO.getPage();
            this.size = pageRequestDTO.getSize();
            this.totalCount = result.getTotalElements();

            // 검색 조건 유지
            this.searchType = pageRequestDTO.getSearchType();
            this.searchKeyword = pageRequestDTO.getSearchKeyword();
            this.sortField = pageRequestDTO.getSortField();
            this.sortDirection = pageRequestDTO.getSortDirection();
            this.categoryId = pageRequestDTO.getCategoryId();

            // 페이지 번호 계산 (10개씩 표시)
            int tempEnd = (int)(Math.ceil(page / 10.0)) * 10;
            this.start = tempEnd - 9;
            this.end = Math.min(totalPage, tempEnd);
            this.prev = this.start > 1;
            this.next = totalPage > tempEnd;

            if (this.end > 0) {
                this.pageList = IntStream.rangeClosed(start, end).boxed().collect(Collectors.toList());
            } else {
                this.pageList = List.of(1);
            }
        }
    }
}