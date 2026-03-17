package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private String profileImageUrl;
    private String province;
    private String district;
    private String city;
    private String street;
}
