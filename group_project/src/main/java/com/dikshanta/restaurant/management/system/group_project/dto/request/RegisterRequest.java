package com.dikshanta.restaurant.management.system.group_project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name length is invalid")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    @Size(max = 254, message = "Email is too long")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password length must be between 6 and 100")
    private String password;

    @NotBlank(message = "Province is required")
    @Size(max = 100, message = "Province is too long")
    private String province;

    @NotBlank(message = "District is required")
    @Size(max = 100, message = "District is too long")
    private String district;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City is too long")
    private String city;

    @NotBlank(message = "Street is required")
    @Size(max = 200, message = "Street is too long")
    private String street;


}
