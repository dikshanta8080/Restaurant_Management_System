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
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {
    private final UserService userService;
    private final RestaurantService restaurantService;

    @GetMapping("/greet")
    public ResponseEntity<String> greetRestaurant() {
        return new ResponseEntity<>("Hello Restaurant", HttpStatus.OK);
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

    @PutMapping("/updateStatus")
    public ResponseEntity<ApiResponse<String>> updateRestaurantStatus(@RequestBody @Valid RestaurantStatusUpdateRequest request) {
        restaurantService.updateRestaurantStatus(request);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .httpStatus(HttpStatus.OK)
                .message("Changed the status of restaurant")
                .responseObject("status updated successfully")
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }
}
