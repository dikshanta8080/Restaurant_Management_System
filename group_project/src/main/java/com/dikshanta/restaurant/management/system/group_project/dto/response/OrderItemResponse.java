package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemResponse {
    private Long id;
    private Long foodItemId;
    private String foodItemName;
    private Integer quantity;
    private BigDecimal price;
}
