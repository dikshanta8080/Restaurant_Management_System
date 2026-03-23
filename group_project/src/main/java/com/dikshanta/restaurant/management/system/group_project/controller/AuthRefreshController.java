package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.response.LoginResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RegisterResponse;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserDoesnotExistsException;
import com.dikshanta.restaurant.management.system.group_project.mappers.response.RegisterResponseMapper;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.model.UserPrincipal;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import com.dikshanta.restaurant.management.system.group_project.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides a "refresh my token" endpoint so that a logged-in user can
 * obtain a new JWT that reflects their current role in the database.
 *
 * Use case: A CUSTOMER registers a restaurant. After admin approval their
 * DB role changes to RESTAURANT. They can call GET /api/v1/auth/refresh
 * (still authenticated with their old CUSTOMER token) to receive a new
 * JWT that carries the RESTAURANT role — without having to log out and
 * log back in manually.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRefreshController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RegisterResponseMapper responseMapper;

    @GetMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new UserDoesnotExistsException("User not authenticated");
        }

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new UserDoesnotExistsException("User not found"));

        String newJwt = jwtService.getJwt(user);
        RegisterResponse userResponse = responseMapper.apply(user);

        LoginResponse loginResponse = LoginResponse.builder()
                .token(newJwt)
                .user(userResponse)
                .build();

        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Token refreshed successfully")
                .responseObject(loginResponse)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
