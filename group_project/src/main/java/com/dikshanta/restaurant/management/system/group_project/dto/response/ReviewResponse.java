package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;
    private Integer rating;
    private String comment;
    private Long userId;
    private String userName;
    private Long restaurantId;
    private Long foodItemId;
    private LocalDateTime createdAt;
}
