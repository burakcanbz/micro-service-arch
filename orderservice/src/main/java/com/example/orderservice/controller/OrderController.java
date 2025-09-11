package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Claims;


import java.util.List;
import java.util.Optional;

@RestController // Bu sınıf REST endpointleri sunuyor
@RequestMapping("/api/orders") // Tüm endpointler /api/orders base path altında
public class OrderController {

    @Autowired
    private JwtService jwtService;

    @Autowired // DI ile service inject ediyoruz
    private OrderService orderService;

    // 1. Belirli bir kullanıcıya ait tüm siparişleri getir
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable String userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // 2. Tüm siparişleri sayfalama (pagination) ile getir
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Order> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    // 3. Belirli bir siparişi ID ile getir
    @GetMapping("/status/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderStatus> getOrderStatusByOrderId(@PathVariable Long orderId) {
        Optional<OrderStatus> optionalOrderStatus = orderService.getOrderStatusByOrderId(orderId);
        return optionalOrderStatus.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 4. Yeni bir sipariş oluştur
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String authHeader, @RequestBody Order order) {
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
        }
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        Integer userId = claims.get("id", Integer.class);
        order.setUserId(String.valueOf(userId));
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.ok(createdOrder);
    }

    // 5. Mevcut bir siparişi iptal et
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long orderId) {
        Optional<Order> optionalOrder = orderService.cancelOrder(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Order> addOrderItemToOrder(@PathVariable Long orderId, OrderItem newItem) {
        Optional<Order> optionalAddedOrder = orderService.addOrderItemToOrder(orderId, newItem);
        return optionalAddedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {

        Optional<Order> updatedOrder = orderService.removeItemFromOrder(orderId, itemId);

        return updatedOrder
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
