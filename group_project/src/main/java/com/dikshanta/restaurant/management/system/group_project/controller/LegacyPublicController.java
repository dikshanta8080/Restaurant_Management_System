package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.response.FoodItemResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantResponse;
import com.dikshanta.restaurant.management.system.group_project.service.FoodItemService;
import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Backwards-compatible public endpoints.
 * Some older frontends expect these routes without the /api/v1 prefix.
 */
@RestController
@RequiredArgsConstructor
public class LegacyPublicController {

    private final RestaurantService restaurantService;
    private final FoodItemService foodItemService;


    @GetMapping("/restaurants")
    public ResponseEntity<List<RestaurantResponse>> restaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurants(search, minRating, sortBy, sortDir));
    }


    @GetMapping("/restaurants/approved")
    public ResponseEntity<List<RestaurantResponse>> approvedRestaurants(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minRating,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(restaurantService.getRestaurants(search, minRating, sortBy, sortDir));
    }


    @GetMapping("/foods")
    public ResponseEntity<Page<FoodItemResponse>> foods(
            @RequestParam(defaultValue = "0") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return ResponseEntity.ok(foodItemService.getAllFoodItems(null, null, null, null, PageRequest.of(Math.max(pageNo, 0), pageSize, sort)));
    }


    @GetMapping("/foods/all")
    public ResponseEntity<List<FoodItemResponse>> allFoods() {
        return ResponseEntity.ok(
                foodItemService.getAllFoodItems(null, null, null, null, PageRequest.of(0, Integer.MAX_VALUE))
                        .getContent()
        );
    }
}

