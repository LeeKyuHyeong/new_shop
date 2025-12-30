package com.kh.shop.service;

import com.kh.shop.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsService {

    private final OrderItemRepository orderItemRepository;

    public List<Map<String, Object>> getCategoryStats(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = orderItemRepository .findCategoryStatsByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("categoryName", result[0] != null ? result[0] : "미분류");
                    map.put("totalQuantity", result[1]);
                    map.put("totalAmount", result[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getColorStats(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = orderItemRepository.findColorStatsByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("color", result[0] != null ? result[0] : "색상없음");
                    map.put("totalQuantity", result[1]);
                    map.put("totalAmount", result[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getSizeStats(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = orderItemRepository.findSizeStatsByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        return results.stream()
                .map(result -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("size", result[0] != null ? result[0] : "사이즈없음");
                    map.put("totalQuantity", result[1]);
                    map.put("totalAmount", result[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }
}