package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.response.CheckoutSessionResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus;
import com.dikshanta.restaurant.management.system.group_project.dto.response.PaymentResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.PaymentStatus;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Order;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Payment;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.OrderRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.PaymentRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final SecurityAuditorAware securityAuditorAware;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${stripe.success-url}")
    private String stripeSuccessUrl;

    @Value("${stripe.cancel-url}")
    private String stripeCancelUrl;

    @PostConstruct
    public void initStripe() {
        Stripe.apiKey = stripeSecretKey;
    }

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

    @Transactional
    public CheckoutSessionResponse createCheckoutSession(Long orderId) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        validateOrderAccess(order, user);
        if (order.getTotalPrice() == null || order.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Order total must be greater than zero");
        }

        // Keep one latest pending payment record linked to this Stripe checkout flow.
        Payment payment = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId).orElse(
                Payment.builder()
                        .order(order)
                        .user(user)
                        .amount(order.getTotalPrice())
                        .status(PaymentStatus.PENDING)
                        .build()
        );
        payment.setUser(user);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentTimestamp(null);
        payment.setTransactionId(null);
        paymentRepository.save(payment);

        try {
            SessionCreateParams.LineItem.PriceData.ProductData productData = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName("FoodHub Order #" + order.getId())
                    .build();
            SessionCreateParams.LineItem.PriceData priceData = SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("npr")
                    .setUnitAmount(order.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValueExact())
                    .setProductData(productData)
                    .build();

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(stripeSuccessUrl + "?order_id=" + order.getId() + "&session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(stripeCancelUrl + "?order_id=" + order.getId())
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("userId", user.getId().toString())
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setQuantity(1L)
                                    .setPriceData(priceData)
                                    .build()
                    )
                    .build();

            Session session = Session.create(params);
            return CheckoutSessionResponse.builder()
                    .checkoutUrl(session.getUrl())
                    .sessionId(session.getId())
                    .build();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe checkout session creation failed: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse markOrderPaid(Long orderId, String transactionId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        User user = order.getUser();

        Payment payment = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)
                .orElse(Payment.builder()
                        .order(order)
                        .user(user)
                        .amount(order.getTotalPrice())
                        .status(PaymentStatus.PENDING)
                        .build());

        payment.setUser(user);
        payment.setAmount(order.getTotalPrice());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentTimestamp(LocalDateTime.now());
        payment.setTransactionId(transactionId);

        order.setStatus(OrderStatus.PAID);
        if (order.getOrderItems() != null) {
            order.getOrderItems().forEach(item -> item.setStatus(OrderStatus.PAID));
        }
        orderRepository.save(order);
        return map(paymentRepository.save(payment));
    }

    @Transactional
    public void handleWebhook(String payload, String stripeSignature) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            throw new RuntimeException("Stripe webhook secret is not configured");
        }
        try {
            Event event = Webhook.constructEvent(payload, stripeSignature, stripeWebhookSecret);
            if ("checkout.session.completed".equals(event.getType())) {
                StripeObject stripeObject = event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new RuntimeException("Unable to deserialize Stripe session object"));
                Session session = (Session) stripeObject;
                String orderIdRaw = session.getMetadata().get("orderId");
                if (orderIdRaw == null || orderIdRaw.isBlank()) {
                    throw new RuntimeException("Missing orderId metadata in Stripe session");
                }
                Long orderId = Long.valueOf(orderIdRaw);
                String transactionId = session.getPaymentIntent();
                if (transactionId == null || transactionId.isBlank()) {
                    transactionId = session.getId();
                }
                markOrderPaid(orderId, transactionId);
            }
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe webhook signature");
        } catch (StripeException e) {
            throw new RuntimeException("Stripe webhook processing failed: " + e.getMessage());
        }
    }

    public PaymentResponse getPaymentByOrder(Long orderId) {
        Payment payment = paymentRepository.findFirstByOrderIdOrderByIdDesc(orderId)
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
