package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantStatusUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserDeleteRequest;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import com.dikshanta.restaurant.management.system.group_project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping("/greet")
    public ResponseEntity<String> greetAdmin() {
        return new ResponseEntity<>("Hello Admin", HttpStatus.OK);
    }

    @PutMapping("/updateStatus")
    public ResponseEntity<ApiResponse<String>> updateRestaurantStatus(
            @RequestBody @Valid RestaurantStatusUpdateRequest request) {
        restaurantService.updateRestaurantStatus(request);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .httpStatus(HttpStatus.OK)
                .message("Changed the status of restaurant")
                .responseObject("Status updated successfully")
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }

    @DeleteMapping("/deleteUser")
    public ResponseEntity<ApiResponse<String>> deleteUser(@Valid @RequestBody UserDeleteRequest request) {
        userService.deleteUser(request);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .httpStatus(HttpStatus.OK)
                .message("Successfully deleted the user")
                .responseObject("User deleted!")
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }
}
