package com.kh.shop.controller.admin;

import com.kh.shop.entity.Order;
import com.kh.shop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    // 주문 목록
    @GetMapping
    public String orderList(@RequestParam(required = false) String status, Model model, HttpSession session) {
        String loggedInUser = (String) session.getAttribute("loggedInUser");
        String userRole = (String) session.getAttribute("userRole");

        if (loggedInUser == null || !"ADMIN".equals(userRole)) {
            return "redirect:/login";
        }

        List<Order> orders;
        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }

        model.addAttribute("orders", orders);
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