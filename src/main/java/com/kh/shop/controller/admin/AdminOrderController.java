package com.kh.shop.controller.admin;

import com.kh.shop.entity.Order;
import com.kh.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // 주문 목록 (페이징 적용 - 전체 메모리 로드 방지)
    @GetMapping
    public String orderList(@RequestParam(required = false) String status,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size,
                            Model model, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            return "redirect:/login";
        }

        // page size 상한 (악의적 거대 size 요청 차단)
        if (size > 100) size = 100;
        if (size < 1) size = 20;
        if (page < 0) page = 0;

        Page<Order> ordersPage;
        if (status != null && !status.isEmpty()) {
            ordersPage = orderService.getOrdersByStatusPaged(status, page, size);
        } else {
            ordersPage = orderService.getAllOrdersPaged(page, size);
        }

        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalElements", ordersPage.getTotalElements());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("activeMenu", "order");

        return "admin/order/list";
    }

    // 주문 상세
    @GetMapping("/detail/{orderId}")
    public String orderDetail(@PathVariable Long orderId, Model model, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null) {
            return "redirect:/admin/order";
        }

        model.addAttribute("order", order);
        model.addAttribute("activeMenu", "order");

        return "admin/order/detail";
    }

    // 주문 상태 변경 (AJAX)
    @PostMapping("/status/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long orderId,
            @RequestParam String status,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Order order = orderService.updateOrderStatus(orderId, status);
            response.put("success", true);
            response.put("message", "주문 상태가 변경되었습니다.");
            response.put("statusName", order.getOrderStatusName());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 주문 취소 (AJAX)
    @PostMapping("/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancelReason,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            orderService.cancelOrder(orderId, cancelReason);
            response.put("success", true);
            response.put("message", "주문이 취소되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 주문 삭제 (AJAX)
    @DeleteMapping("/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteOrder(
            @PathVariable Long orderId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            response.put("success", false);
            response.put("message", "권한이 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            orderService.deleteOrder(orderId);
            response.put("success", true);
            response.put("message", "주문이 삭제되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}