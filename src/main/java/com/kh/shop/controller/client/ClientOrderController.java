package com.kh.shop.controller.client;

import com.kh.shop.entity.Cart;
import com.kh.shop.entity.Order;
import com.kh.shop.entity.Product;
import com.kh.shop.service.CartService;
import com.kh.shop.service.CategoryService;
import com.kh.shop.service.OrderService;
import com.kh.shop.service.ProductService;
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
public class ClientOrderController {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // ==================== 장바구니 ====================

    // 장바구니 페이지
    @GetMapping("/cart")
    public String cart(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login?redirect=/cart";
        }

        List<Cart> cartItems = cartService.getCartByUser(userId);
        int totalPrice = cartItems.stream().mapToInt(Cart::getTotalPrice).sum();
        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalPrice", totalPrice + deliveryFee);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/cart";
    }

    // 장바구니 추가 (AJAX)
    @PostMapping("/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            response.put("needLogin", true);
            return ResponseEntity.ok(response);
        }

        try {
            cartService.addToCart(userId, productId, quantity);
            long cartCount = cartService.getCartCount(userId);
            response.put("success", true);
            response.put("message", "장바구니에 추가되었습니다.");
            response.put("cartCount", cartCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 장바구니 수량 변경 (AJAX)
    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartQuantity(
            @RequestParam Long cartId,
            @RequestParam Integer quantity,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        try {
            Cart cart = cartService.updateQuantity(cartId, quantity, userId);
            response.put("success", true);
            response.put("itemTotal", cart.getTotalPrice());
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 장바구니 삭제 (AJAX)
    @DeleteMapping("/cart/{cartId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @PathVariable Long cartId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        try {
            cartService.removeFromCart(cartId, userId);
            response.put("success", true);
            response.put("message", "삭제되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // 장바구니 수량 조회 (AJAX - 헤더용)
    @GetMapping("/cart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            response.put("count", 0);
        } else {
            response.put("count", cartService.getCartCount(userId));
        }

        return ResponseEntity.ok(response);
    }

    // ==================== 주문 ====================

    // 주문 페이지 (장바구니에서)
    @PostMapping("/order/checkout")
    public String checkout(@RequestParam List<Long> cartIds, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login?redirect=/cart";
        }

        List<Cart> cartItems = cartService.getCartByUser(userId).stream()
                .filter(c -> cartIds.contains(c.getCartId()))
                .toList();

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        int totalPrice = cartItems.stream().mapToInt(Cart::getTotalPrice).sum();
        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartIds", cartIds);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalPrice", totalPrice + deliveryFee);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/checkout";
    }

    // 바로 구매 페이지
    @GetMapping("/order/direct")
    public String directOrder(@RequestParam Long productId,
                              @RequestParam(defaultValue = "1") Integer quantity,
                              Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login?redirect=/product/" + productId;
        }

        Product product = productService.getProductById(productId).orElse(null);
        if (product == null) {
            return "redirect:/";
        }

        if (product.getProductStock() < quantity) {
            return "redirect:/product/" + productId + "?error=stock";
        }

        int totalPrice = product.getDiscountedPrice() * quantity;
        int deliveryFee = totalPrice >= 50000 ? 0 : 3000;

        model.addAttribute("product", product);
        model.addAttribute("quantity", quantity);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("finalPrice", totalPrice + deliveryFee);
        model.addAttribute("isDirect", true);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/checkout";
    }

    // 주문 처리
    @PostMapping("/order/submit")
    public String submitOrder(
            @RequestParam(required = false) List<Long> cartIds,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer quantity,
            @RequestParam String receiverName,
            @RequestParam String receiverPhone,
            @RequestParam String postalCode,
            @RequestParam String receiverAddress,
            @RequestParam(required = false) String receiverAddressDetail,
            @RequestParam(required = false) String orderMemo,
            @RequestParam String paymentMethod,
            HttpSession session) {

        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            Order order;
            if (productId != null && quantity != null) {
                // 바로 구매
                order = orderService.createDirectOrder(userId, productId, quantity,
                        receiverName, receiverPhone, postalCode, receiverAddress,
                        receiverAddressDetail, orderMemo, paymentMethod);
            } else {
                // 장바구니 주문
                order = orderService.createOrderFromCart(userId, cartIds,
                        receiverName, receiverPhone, postalCode, receiverAddress,
                        receiverAddressDetail, orderMemo, paymentMethod);
            }

            return "redirect:/order/complete/" + order.getOrderId();
        } catch (Exception e) {
            return "redirect:/cart?error=" + e.getMessage();
        }
    }

    // 주문 완료 페이지
    @GetMapping("/order/complete/{orderId}")
    public String orderComplete(@PathVariable Long orderId, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null || !order.getUser().getUserId().equals(userId)) {
            return "redirect:/";
        }

        model.addAttribute("order", order);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/order-complete";
    }

    // 주문 내역 목록
    @GetMapping("/mypage/orders")
    public String myOrders(Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login?redirect=/mypage/orders";
        }

        List<Order> orders = orderService.getOrdersByUser(userId);
        model.addAttribute("orders", orders);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/my-orders";
    }

    // 주문 상세
    @GetMapping("/mypage/order/{orderId}")
    public String myOrderDetail(@PathVariable Long orderId, Model model, HttpSession session) {
        String userId = (String) session.getAttribute("loggedInUser");
        if (userId == null) {
            return "redirect:/login";
        }

        Order order = orderService.getOrderById(orderId).orElse(null);
        if (order == null || !order.getUser().getUserId().equals(userId)) {
            return "redirect:/mypage/orders";
        }

        model.addAttribute("order", order);
        model.addAttribute("parentCategories", categoryService.getParentCategoriesWithChildren());

        return "client/order-detail";
    }

    // 주문 취소 (AJAX)
    @PostMapping("/mypage/order/cancel/{orderId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelMyOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String cancelReason,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();
        String userId = (String) session.getAttribute("loggedInUser");

        if (userId == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.ok(response);
        }

        try {
            Order order = orderService.getOrderById(orderId).orElse(null);
            if (order == null || !order.getUser().getUserId().equals(userId)) {
                response.put("success", false);
                response.put("message", "주문을 찾을 수 없습니다.");
                return ResponseEntity.ok(response);
            }

            orderService.cancelOrder(orderId, cancelReason);
            response.put("success", true);
            response.put("message", "주문이 취소되었습니다.");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}