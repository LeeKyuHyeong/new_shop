package com.kh.shop.controller.admin;

import com.kh.shop.entity.ProfanityWord;
import com.kh.shop.service.ProfanityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/profanity")
@RequiredArgsConstructor
public class AdminProfanityController {

    private final ProfanityService profanityService;

    /**
     * 비속어 목록 페이지
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        Page<ProfanityWord> wordPage;
        if ((category != null && !category.isEmpty()) || (keyword != null && !keyword.isEmpty())) {
            wordPage = profanityService.search(category, keyword, page, size);
        } else {
            wordPage = profanityService.getList(page, size);
        }

        model.addAttribute("words", wordPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", wordPage.getTotalPages());
        model.addAttribute("totalElements", wordPage.getTotalElements());
        model.addAttribute("category", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", profanityService.getCategories());
        model.addAttribute("stats", profanityService.getStats());

        return "admin/profanity/list";
    }
}