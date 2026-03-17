package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {
    private final RestaurantService restaurantService;

    @GetMapping("/greet")
    public ResponseEntity<String> greetCustomer() {
        return new ResponseEntity<>("Hello Customer", HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<RestaurantCreateResponse>> createRestaurant(@RequestBody @Valid RestaurantCreateRequest request) {
        RestaurantCreateResponse restaurant = restaurantService.createRestaurant(request);
        ApiResponse<RestaurantCreateResponse> apiResponse = ApiResponse.<RestaurantCreateResponse>builder()
                .httpStatus(HttpStatus.CREATED)
                .message("Restaurant creation form filled!")
                .responseObject(restaurant)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }
}
