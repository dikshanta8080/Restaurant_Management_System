package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserProfileUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.UserResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import com.dikshanta.restaurant.management.system.group_project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final RestaurantService restaurantService;
    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        UserResponse userResponse = userService.getUserProfile();
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Profile fetched successfully")
                .responseObject(userResponse)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/createRestaurant", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RestaurantCreateResponse>> createRestaurant(
            @ModelAttribute RestaurantCreateRequest request
    ) {
        RestaurantCreateResponse restaurant = restaurantService.createRestaurant(request);
        System.out.println(restaurant.getName());

        ApiResponse<RestaurantCreateResponse> apiResponse = ApiResponse.<RestaurantCreateResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Restaurant creation form filled!")
                .responseObject(restaurant)
                .build();

        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody @Valid UserProfileUpdateRequest request) {
        UserResponse updatedProfile = userService.updateUserProfile(request);
        ApiResponse<UserResponse> apiResponse = ApiResponse.<UserResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Profile updated successfully")
                .responseObject(updatedProfile)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/greet")
    public ResponseEntity<String> greetCustomer() {
        return new ResponseEntity<>("Hello Customer", HttpStatus.OK);
    }


    @GetMapping("/allRestaurants")
    public ResponseEntity<ApiResponse<List<RestaurantResponse>>> getAllRestaurants() {
        List<RestaurantResponse> restaurants = restaurantService.getRestaurants();
        ApiResponse<List<RestaurantResponse>> apiResponse = ApiResponse.<List<RestaurantResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Parsed the restaurants")
                .responseObject(restaurants)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());

    }
}
