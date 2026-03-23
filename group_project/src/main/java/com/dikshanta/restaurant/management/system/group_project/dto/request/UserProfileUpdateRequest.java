package com.dikshanta.restaurant.management.system.group_project.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String profileImageUrl;


    private String province;
    private String district;
    private String city;
    private String street;
}
