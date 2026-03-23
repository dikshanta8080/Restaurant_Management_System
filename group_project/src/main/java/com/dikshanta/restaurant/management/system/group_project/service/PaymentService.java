package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.response.PaymentResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.PaymentStatus;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Order;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Payment;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.OrderRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.PaymentRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SecurityAuditorAware securityAuditorAware;

    private Long getCurrentUserId() {
        return securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
    }

    private PaymentResponse map(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentTimestamp(payment.getPaymentTimestamp())
                .build();
    }

    private void validateOrderAccess(Order order, User user) {
        boolean isCustomer = order.getUser() != null && order.getUser().getId().equals(user.getId());
        if (!isCustomer && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Not authorized to pay for this order");
        }
    }

    public PaymentResponse payNowDummy(Long orderId) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        validateOrderAccess(order, user);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElse(Payment.builder()
                        .order(order)
                        .user(user)
                        .amount(order.getTotalPrice())
                        .status(PaymentStatus.PENDING)
                        .build());
        payment.setUser(user);
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentTimestamp(LocalDateTime.now());
        payment.setTransactionId("DUMMY-" + orderId + "-" + System.currentTimeMillis());

        // Simulated payment completion: move the order forward.
        // This makes the full flow (order -> pay -> completed) observable in UI.
        order.setStatus(com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus.COMPLETED);
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> item.setStatus(com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus.COMPLETED));
        }
        orderRepository.save(order);

        return map(paymentRepository.save(payment));
    }

    public PaymentResponse getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order"));
        return map(payment);
    }

    public List<PaymentResponse> getMyPayments() {
        Long userId = getCurrentUserId();
        return paymentRepository.findByUserId(userId).stream()
                .map(this::map)
                .toList();
    }

    public List<PaymentResponse> getOwnerPayments() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.RESTAURANT && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Not authorized to view restaurant payments");
        }
        return (user.getRole() == Role.ADMIN ? paymentRepository.findAll() : paymentRepository.findForRestaurantOwner(userId))
                .stream().map(this::map).toList();
    }

    public List<PaymentResponse> getAllPaymentsForAdmin() {
        Long userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can view all payments");
        }
        return paymentRepository.findAll().stream()
                .map(this::map)
                .toList();
    }
}
