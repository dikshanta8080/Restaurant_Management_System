package com.dikshanta.restaurant.management.system.group_project.dto.request;

import com.dikshanta.restaurant.management.system.group_project.validators.EmailValidator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @EmailValidator
    private String email;
    @NotEmpty(message = "Password can not be empty")
    @NotNull(message = "Password can not be empty")
    private String password;
}
