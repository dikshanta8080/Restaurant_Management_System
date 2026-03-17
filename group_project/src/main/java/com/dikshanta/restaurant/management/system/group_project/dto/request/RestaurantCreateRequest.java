package com.dikshanta.restaurant.management.system.group_project.dto.request;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {
    private String name;
    private String description;
    private String province;
    private String district;
    private String city;
    private String street;


}