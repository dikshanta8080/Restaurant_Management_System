package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private String ownerName;
    private RestaurantStatus status;
    private Double averageRating;
    private Integer reviewCount;
}
