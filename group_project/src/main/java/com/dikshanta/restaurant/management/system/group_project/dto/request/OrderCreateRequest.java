package com.dikshanta.restaurant.management.system.group_project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    @NotNull(message = "Order items are required")
    @NotEmpty(message = "Order must contain at least one item")
    @Valid
    private List<OrderItemRequest> items;

    @Positive(message = "Address id must be positive")
    private Long addressId;


    @Size(max = 100, message = "Province is too long")
    private String province;

    @Size(max = 100, message = "District is too long")
    private String district;

    @Size(max = 100, message = "City is too long")
    private String city;

    @Size(max = 200, message = "Street is too long")
    private String street;
}
