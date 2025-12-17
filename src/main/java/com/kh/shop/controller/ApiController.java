package com.kh.shop.controller;

import com.kh.shop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Autowired
    private UserService userService;

    @GetMapping("/check-userid")
    public Map<String, Object> checkUserId(@RequestParam String userId) {
        Map<String, Object> response = new HashMap<>();
        boolean duplicate = userService.isDuplicateUserId(userId);
        response.put("duplicate", duplicate);
        return response;
    }

    @GetMapping("/check-email")
    public Map<String, Object> checkEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        boolean duplicate = userService.isDuplicateEmail(email);
        response.put("duplicate", duplicate);
        return response;
    }
}