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
import java.util.EnumSet;
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
                .createdAt(order.getCreatedAt())
                .userId(order.getUser().getId())
                .build();
    }

    private Address resolveDeliveryAddress(User user, Long addressId) {
        if (addressId != null) {
            return addressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Address not found"));
        }
        if (user.getAddress() != null) {
            return user.getAddress();
        }
        throw new RuntimeException("Delivery address is required");
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus nextStatus) {
        if (currentStatus == nextStatus) {
            return;
        }
        boolean valid = switch (currentStatus) {
            case PENDING -> EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.REJECTED).contains(nextStatus);
            case ACCEPTED -> nextStatus == OrderStatus.COMPLETED;
            case COMPLETED, REJECTED -> false;
        };
        if (!valid) {
            throw new RuntimeException("Invalid order status transition: " + currentStatus + " -> " + nextStatus);
        }
    }

    @Transactional
    public OrderResponse placeOrder(OrderCreateRequest request) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = resolveDeliveryAddress(user, request.getAddressId());

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

    public Page<OrderResponse> getOwnerIncomingOrders(Pageable pageable) {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.RESTAURANT && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only restaurant owners and admins can view incoming orders");
        }

        Page<Order> orders = user.getRole() == Role.ADMIN
                ? orderRepository.findAll(pageable)
                : orderRepository.findDistinctByOrderItemsRestaurantOwnerId(userId, pageable);
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

        validateStatusTransition(order.getStatus(), status);
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
