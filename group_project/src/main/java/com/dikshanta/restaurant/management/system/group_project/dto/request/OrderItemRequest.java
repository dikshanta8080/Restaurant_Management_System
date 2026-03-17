package com.dikshanta.restaurant.management.system.group_project.dto.request;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long foodItemId;
    private Integer quantity;
}
