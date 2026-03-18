package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/restaurant")
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping("/greet")
    public ResponseEntity<String> greetRestaurant() {
        return new ResponseEntity<>("Hello Restaurant", HttpStatus.OK);
    }

    @PutMapping(value = "/updateRestaurant/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RestaurantCreateResponse>> updateRestaurant(
            @PathVariable Long id,
            @ModelAttribute @Valid RestaurantCreateRequest request) {
        RestaurantCreateResponse restaurant = restaurantService.updateRestaurant(id, request);
        ApiResponse<RestaurantCreateResponse> apiResponse = ApiResponse.<RestaurantCreateResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Restaurant updated successfully")
                .responseObject(restaurant)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @DeleteMapping("/deleteRestaurant/{id}")
    public ResponseEntity<ApiResponse<String>> deleteRestaurant(@PathVariable Long id) {
        restaurantService.deleteRestaurant(id);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .httpStatus(HttpStatus.OK)
                .message("Restaurant deleted successfully")
                .responseObject("Deleted!")
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @GetMapping("/getRestaurant")
    public ResponseEntity<ApiResponse<RestaurantResponse>> getRestaurant() {
        RestaurantResponse ownRestaurant = restaurantService.getOwnRestaurant();
        ApiResponse<RestaurantResponse> apiResponse = ApiResponse.<RestaurantResponse>builder()
                .httpStatus(HttpStatus.OK)
                .message("Parsed the restaurant successfully")
                .responseObject(ownRestaurant)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());
    }
}
