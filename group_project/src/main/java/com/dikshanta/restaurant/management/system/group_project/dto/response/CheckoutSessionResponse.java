package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutSessionResponse {
    private String checkoutUrl;
    private String sessionId;
}
