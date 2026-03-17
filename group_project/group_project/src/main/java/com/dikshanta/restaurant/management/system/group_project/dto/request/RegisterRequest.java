package com.dikshanta.restaurant.management.system.group_project.dto.request;

import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.validators.EmailValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {
    @NotNull(message = "Name can not be null")
    @NotEmpty(message = "Name can not be empty")
    private String name;
    @EmailValidator
    private String email;
    @NotNull(message = "Password can not be null")
    @NotEmpty(message = "Password can not be empty")
    private String password;
    @NotNull(message = "Role can not be empty")
    private Role role;

}
