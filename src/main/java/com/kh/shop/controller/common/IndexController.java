package com.kh.shop.controller.common;

import com.kh.shop.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

//    @GetMapping({"/", "/index"})
//    public String index(HttpSession session, Model model) {
//        Object loggedInUser = session.getAttribute("loggedInUser");
//
//        List<Category> categories = categoryService.getAllCategories();
//        model.addAttribute("categories", categories);
//        model.addAttribute("loggedInUser", loggedInUser);
//
//        return "client/main";
//    }
}