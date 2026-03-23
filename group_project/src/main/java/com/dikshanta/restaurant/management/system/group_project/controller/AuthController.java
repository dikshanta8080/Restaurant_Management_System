package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.LoginRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.LoginResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RegisterResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> createUser(@RequestBody @Valid RegisterRequest registerRequest) {
        RegisterResponse response = authService.registerUser(registerRequest);
        ApiResponse<RegisterResponse> apiResponse = ApiResponse.<RegisterResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Successfully Created the user")
                .responseObject(response)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateUser(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.authenticateUser(loginRequest);
        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Login Successful")
                .responseObject(loginResponse)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());

    }
}
