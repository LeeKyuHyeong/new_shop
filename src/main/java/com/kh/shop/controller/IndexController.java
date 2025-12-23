package com.kh.shop.controller;

import com.kh.shop.entity.Category;
import com.kh.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping({"/", "/index"})
    public String index(HttpSession session, Model model) {
        Object loggedInUser = session.getAttribute("loggedInUser");

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("loggedInUser", loggedInUser);

        return "client/index";
    }
}