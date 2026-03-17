package com.dikshanta.restaurant.management.system.group_project.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private RegisterResponse user;

}
