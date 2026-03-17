package com.dikshanta.restaurant.management.system.group_project.dto.request;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantStatusUpdateRequest {
    private Long restaurantId;
    private RestaurantStatus restaurantStatus;
}
