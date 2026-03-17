package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserDeleteRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
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


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RestaurantCreateResponse>> updateRestaurant(@PathVariable Long id, @RequestBody @Valid RestaurantCreateRequest request) {
        RestaurantCreateResponse restaurant = restaurantService.updateRestaurant(id, request);
        ApiResponse<RestaurantCreateResponse> apiResponse = ApiResponse.<RestaurantCreateResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Restaurant updated successfully")
                .responseObject(restaurant)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .httpStatus(HttpStatus.OK)
                .message("Restaurant deleted successfully")
                .responseObject("Deleted!")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
