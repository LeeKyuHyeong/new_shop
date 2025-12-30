package com.kh.shop.controller.admin;

import com.kh.shop.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/stats")
@RequiredArgsConstructor
public class AdminStatsController {

    private final StatsService statsService;

    @GetMapping("")
    public String statsPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("userRole");
        if (role == null || !"ADMIN".equals(role)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("activeMenu", "stats");
        return "admin/stats/list";
    }

    @GetMapping("/api/category")
    @ResponseBody
    public Map<String, Object> getCategoryStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> stats = statsService.getCategoryStats(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        return result;
    }

    @GetMapping("/api/color")
    @ResponseBody
    public Map<String, Object> getColorStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> stats = statsService.getColorStats(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        return result;
    }

    @GetMapping("/api/size")
    @ResponseBody
    public Map<String, Object> getSizeStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Map<String, Object>> stats = statsService.getSizeStats(startDate, endDate);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", stats);
        return result;
    }
}