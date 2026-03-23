package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime paymentTimestamp;
}
