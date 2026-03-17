package com.dikshanta.restaurant.management.system.group_project.mappers.response;

import com.dikshanta.restaurant.management.system.group_project.dto.response.RegisterResponse;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class RegisterResponseMapper implements Function<User, RegisterResponse> {
    @Override
    public RegisterResponse apply(User user) {
        return RegisterResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
