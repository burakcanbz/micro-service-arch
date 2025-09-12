package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.service.OrderService;
import com.example.orderservice.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.jsonwebtoken.Claims;


import java.util.List;
import java.util.Optional;

@RestController // Bu sınıf REST endpointleri sunuyor
@RequestMapping("/orders") // Tüm endpointler /api/orders base path altında
public class OrderController {

    @Autowired
    private JwtService jwtService;

    @Autowired // DI ile service inject ediyoruz
    private OrderService orderService;

    // 1. Belirli bir kullanıcıya ait tüm siparişleri getir
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@RequestHeader("Authorization") String authHeader, @PathVariable String userId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return ResponseEntity.ok(orders);
    }

    // 2. Tüm siparişleri sayfalama (pagination) ile getir
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(@RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Page<Order> orders = orderService.getAllOrders(page, size);
        return ResponseEntity.ok(orders);
    }

    // 3. Belirli bir siparişin status'ünü order ID ile getir
    @GetMapping("/status/{orderId}")
    public ResponseEntity<Order> getOrderStatusByOrderId(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.getOrderById(orderId);
        if(!optionalOrder.isPresent()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(optionalOrder.get());
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
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalOrder = orderService.cancelOrder(orderId);
        return optionalOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping("/addProduct/{orderId}")
    public ResponseEntity<Order> addOrderItemToOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId, @RequestBody OrderItem newItem) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> optionalAddedOrder = orderService.addOrderItemToOrder(orderId, newItem);
        return optionalAddedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItemFromOrder(@RequestHeader("Authorization") String authHeader,
            @PathVariable Long orderId,
            @PathVariable Long itemId) {
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<Order> updatedOrder = orderService.removeItemFromOrder(orderId, itemId);

        return updatedOrder
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<String> removeOrder(@RequestHeader("Authorization") String authHeader, @PathVariable Long orderId){
        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtService.verifyToken(token);
        if (claims == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean removed = orderService.removeOrderById(orderId);

        if(removed){
            return ResponseEntity.ok("Order with ID " + orderId + " has been removed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Order with ID " + orderId + " not found.");
        }
    }
}
