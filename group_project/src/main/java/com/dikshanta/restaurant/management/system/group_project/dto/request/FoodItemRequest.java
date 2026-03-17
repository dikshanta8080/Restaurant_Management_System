package com.dikshanta.restaurant.management.system.group_project.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
}
