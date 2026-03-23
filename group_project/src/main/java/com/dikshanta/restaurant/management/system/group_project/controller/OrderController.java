package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.OrderCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.OrderResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus;
import com.dikshanta.restaurant.management.system.group_project.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/customer/orders")
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody OrderCreateRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(request));
    }

    @GetMapping("/customer/orders")
    public ResponseEntity<Page<OrderResponse>> getUserOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(pageable));
    }

    @GetMapping("/restaurant/orders")
    public ResponseEntity<Page<OrderResponse>> getRestaurantIncomingOrders(Pageable pageable) {
        return ResponseEntity.ok(orderService.getOwnerIncomingOrders(pageable));
    }

    @GetMapping("/customer/orders/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PutMapping("/restaurant/orders/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
