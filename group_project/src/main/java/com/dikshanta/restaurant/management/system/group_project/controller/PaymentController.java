package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.CreateCheckoutSessionRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.CheckoutSessionResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.PaymentResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/customer/payments/create-checkout-session")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(@RequestBody @Valid CreateCheckoutSessionRequest request) {
        CheckoutSessionResponse response = paymentService.createCheckoutSession(request.getOrderId());
        ApiResponse<CheckoutSessionResponse> apiResponse = ApiResponse.<CheckoutSessionResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Stripe checkout session created")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/customer/payments/order/{orderId}/confirm")
    public ResponseEntity<ApiResponse<PaymentResponse>> markOrderPaid(@PathVariable Long orderId, @RequestParam String transactionId) {
        PaymentResponse response = paymentService.markOrderPaid(orderId, transactionId);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Order marked as paid")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/payments/webhook")
    public ResponseEntity<String> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String stripeSignature
    ) {
        paymentService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok("Webhook processed");
    }

    @GetMapping("/customer/payments/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrder(@PathVariable Long orderId) {
        PaymentResponse response = paymentService.getPaymentByOrder(orderId);
        ApiResponse<PaymentResponse> apiResponse = ApiResponse.<PaymentResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Payment fetched")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/customer/payments/my")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments() {
        List<PaymentResponse> response = paymentService.getMyPayments();
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("My payments fetched")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/restaurant/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getOwnerPayments() {
        List<PaymentResponse> response = paymentService.getOwnerPayments();
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Restaurant payments fetched")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/admin/payments")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAllPaymentsForAdmin() {
        List<PaymentResponse> response = paymentService.getAllPaymentsForAdmin();
        ApiResponse<List<PaymentResponse>> apiResponse = ApiResponse.<List<PaymentResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("All payments fetched")
                .responseObject(response)
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
