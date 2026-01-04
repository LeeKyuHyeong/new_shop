package com.kh.shop.controller.admin;

import com.kh.shop.service.BatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/batch")
@RequiredArgsConstructor
public class AdminBatchController {

    private final BatchService batchService;

    @GetMapping("")
    public String batchList(Model model) {
        model.addAttribute("activeMenu", "batch");
        model.addAttribute("batches", batchService.getAllBatchInfo());
        return "admin/batch/list";
    }
}