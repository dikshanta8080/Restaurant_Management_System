package com.dikshanta.restaurant.management.system.group_project.mappers.request;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class RegisterRequestMapper implements Function<RegisterRequest, User> {
    @Override
    public User apply(RegisterRequest registerRequest) {
        return User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .build();
    }
}
