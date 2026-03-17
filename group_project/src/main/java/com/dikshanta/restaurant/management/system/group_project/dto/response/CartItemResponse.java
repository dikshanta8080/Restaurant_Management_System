package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private Long foodItemId;
    private String foodItemName;
    private BigDecimal price;
    private Integer quantity;
    private Long restaurantId;
}
