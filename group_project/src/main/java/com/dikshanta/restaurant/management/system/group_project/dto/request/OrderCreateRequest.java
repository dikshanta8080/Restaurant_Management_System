package com.dikshanta.restaurant.management.system.group_project.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    private List<OrderItemRequest> items;
    private Long addressId;
}
