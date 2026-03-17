package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String token;
}
