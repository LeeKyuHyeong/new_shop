package com.kh.shop.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequestDTO {

    @Builder.Default
    private int page = 1;

    @Builder.Default
    private int size = 10;

    private String searchType;
    private String searchKeyword;
    private String sortField;
    private String sortDirection;

    // 카테고리 필터용
    private Integer categoryId;

    public Pageable getPageable() {
        return getPageable("productId");
    }

    public Pageable getPageable(String defaultSortField) {
        Sort sort;
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection)
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        } else {
            sort = Sort.by(Sort.Direction.DESC, defaultSortField);
        }
        return PageRequest.of(page - 1, size, sort);
    }

    public String getLink() {
        StringBuilder sb = new StringBuilder();
        sb.append("page=").append(page);
        sb.append("&size=").append(size);
        if (searchType != null && !searchType.isEmpty()) {
            sb.append("&searchType=").append(searchType);
        }
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            sb.append("&searchKeyword=").append(searchKeyword);
        }
        if (sortField != null && !sortField.isEmpty()) {
            sb.append("&sortField=").append(sortField);
        }
        if (sortDirection != null && !sortDirection.isEmpty()) {
            sb.append("&sortDirection=").append(sortDirection);
        }
        if (categoryId != null) {
            sb.append("&categoryId=").append(categoryId);
        }
        return sb.toString();
    }
}