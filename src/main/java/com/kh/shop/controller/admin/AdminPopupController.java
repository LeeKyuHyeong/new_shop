package com.kh.shop.controller.admin;

import com.kh.shop.entity.Popup;
import com.kh.shop.service.PopupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/popup")
public class AdminPopupController {

    @Autowired
    private PopupService popupService;

    private boolean isAdmin(HttpSession session) {
        Object loggedInUser = session.getAttribute("loggedInUser");
        Object userRole = session.getAttribute("userRole");
        return loggedInUser != null && "ADMIN".equals(userRole);
    }

    @GetMapping
    public String popupList(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        List<Popup> popups = popupService.getAllPopups();
        model.addAttribute("popups", popups);
        model.addAttribute("activeMenu", "popup");
        return "admin/popup/list";
    }

    @GetMapping("/create")
    public String popupCreatePage(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("activeMenu", "popup");
        return "admin/popup/form";
    }

    @GetMapping("/edit/{popupId}")
    public String popupEditPage(@PathVariable Long popupId, HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/admin/login";
        }

        Optional<Popup> popup = popupService.getPopupById(popupId);
        if (popup.isPresent()) {
            model.addAttribute("popup", popup.get());
            model.addAttribute("activeMenu", "popup");
            return "admin/popup/form";
        }
        return "redirect:/admin/popup";
    }
}