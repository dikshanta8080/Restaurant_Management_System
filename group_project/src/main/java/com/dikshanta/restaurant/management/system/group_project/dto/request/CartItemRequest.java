package com.dikshanta.restaurant.management.system.group_project.dto.request;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long foodItemId;
    private Integer quantity;
}
