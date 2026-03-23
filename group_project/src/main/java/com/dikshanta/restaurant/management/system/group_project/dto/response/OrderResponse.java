package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private Long userId;

    // Delivery address snapshot (may be null if not provided).
    private Long deliveryAddressId;
    private String deliveryProvince;
    private String deliveryDistrict;
    private String deliveryCity;
    private String deliveryStreet;
}
