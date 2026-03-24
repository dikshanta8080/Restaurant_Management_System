package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class OrderPlacementResponse {
    private List<OrderResponse> orders;
    private BigDecimal totalPrice;
}

