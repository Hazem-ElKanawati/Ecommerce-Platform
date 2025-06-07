package com.example.orderservice.controller;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderItem;
import com.example.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private UUID getAuthenticatedUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            Object details = token.getDetails();
            if (details instanceof UUID id) {
                return id;
            }
        }
        return null;
    }
    @PostMapping
    public ResponseEntity<?> createOrder(
            @RequestParam UUID userId,
            @RequestBody List<OrderItem> items)
    {
        UUID authId = getAuthenticatedUserId();
        if (authId == null || !authId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return orderService.createOrder(userId, items);
    }
    @GetMapping("/test")
    public String testEndpoint() {
        return "Order Service is working!";
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(value = "userId" , required = false) UUID userId) {
        UUID authId = getAuthenticatedUserId();
        if (userId != null) {
            if (!userId.equals(authId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(orderService.getOrdersByUserId(userId));
        }
        return ResponseEntity.ok(orderService.getOrdersByUserId(authId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!order.get().getUserId().equals(getAuthenticatedUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(order.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable UUID id) {
        Optional<Order> order = orderService.getOrderById(id);
        if (order.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!order.get().getUserId().equals(getAuthenticatedUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
