package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.OrderCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.OrderItemResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.OrderResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.*;
import com.dikshanta.restaurant.management.system.group_project.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final FoodItemRepository foodItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final SecurityAuditorAware securityAuditorAware;

    private Long getCurrentUserId() {
        return securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    private OrderItemResponse mapToResponse(OrderItem orderItem) {
        return OrderItemResponse.builder()
                .id(orderItem.getId())
                .foodItemId(orderItem.getFoodItem().getId())
                .foodItemName(orderItem.getFoodItem().getName())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .items(itemResponses)
                .userId(order.getUser().getId())
                .build();
    }

    @Transactional
    public OrderResponse placeOrder(OrderCreateRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }
        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(address);
        order.setStatus(OrderStatus.PENDING);
        order = orderRepository.save(order);

        BigDecimal total = BigDecimal.ZERO;

        for (var itemReq : request.getItems()) {
            FoodItem foodItem = foodItemRepository.findById(itemReq.getFoodItemId())
                    .orElseThrow(() -> new RuntimeException("Food item not found"));

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .foodItem(foodItem)
                    .restaurant(foodItem.getRestaurant())
                    .quantity(itemReq.getQuantity())
                    .price(foodItem.getPrice())
                    .status(OrderStatus.PENDING)
                    .build();

            total = total.add(foodItem.getPrice().multiply(new BigDecimal(itemReq.getQuantity())));
            order.addOrderItem(orderItem);
        }
        order.setTotalPrice(total);
        order = orderRepository.save(order);

        cartService.clearCart(userId);

        return mapToResponse(order);
    }

    public Page<OrderResponse> getUserOrders(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<Order> orders = orderRepository.findByUserId(userId, pageable);
        return orders.map(this::mapToResponse);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        boolean isCustomer = order.getUser().getId().equals(userId);
        boolean isRestaurantOwner = order.getOrderItems().stream()
                .anyMatch(item -> item.getRestaurant() != null && item.getRestaurant().getOwner().getId().equals(userId));

        if (!isCustomer && !isRestaurantOwner && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Not authorized to view this order");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        Long userId = getCurrentUserId();

        boolean isRestaurantOwner = order.getOrderItems().stream()
                .anyMatch(item -> item.getRestaurant() != null && item.getRestaurant().getOwner().getId().equals(userId));

        User user = userRepository.findById(userId).orElseThrow();
        if (!isRestaurantOwner && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Not authorized to update order status");
        }

        order.setStatus(status);
        order.getOrderItems().forEach(item -> item.setStatus(status));
        return mapToResponse(orderRepository.save(order));
    }

    @Transactional
    public void calculateTotals(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        BigDecimal total = order.getOrderItems().stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalPrice(total);
        orderRepository.save(order);
    }
}
