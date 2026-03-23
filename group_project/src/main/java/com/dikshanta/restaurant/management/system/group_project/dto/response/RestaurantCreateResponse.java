package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RestaurantCreateResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private RestaurantStatus status;
    private Long ownerId;
    private Long addressId;
}
