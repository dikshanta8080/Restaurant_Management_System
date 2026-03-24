package com.dikshanta.restaurant.management.system.group_project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCheckoutSessionRequest {
    @NotNull(message = "orderId is required")
    private Long orderId;
}
