package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private RestaurantStatus status;
    private Long ownerId;
    private Long addressId;
}
