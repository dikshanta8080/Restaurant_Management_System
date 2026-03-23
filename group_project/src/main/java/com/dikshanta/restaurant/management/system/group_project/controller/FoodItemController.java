package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.FoodItemRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.CategoryResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.FoodItemResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.service.CategoryService;
import com.dikshanta.restaurant.management.system.group_project.service.FoodItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurant")
@RequiredArgsConstructor
public class FoodItemController {
    private final CategoryService categoryService;

    private final FoodItemService foodItemService;

    @PostMapping(value = "/restaurants/{restaurantId}/food-items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodItemResponse> addFoodItem(
            @PathVariable Long restaurantId,
            @ModelAttribute FoodItemRequest request) {
        FoodItemResponse response = foodItemService.addFoodItem(restaurantId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/food-items/{id}")
    public ResponseEntity<FoodItemResponse> updateFoodItem(
            @PathVariable Long id,
            @RequestBody FoodItemRequest request) {
        FoodItemResponse response = foodItemService.updateFoodItem(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/food-items/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        foodItemService.deleteFoodItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{restaurantId}/food-items")
    public ResponseEntity<List<FoodItemResponse>> getFoodItemsByRestaurant(
            @PathVariable Long restaurantId) {
        List<FoodItemResponse> responses = foodItemService.getFoodItemsByRestaurant(restaurantId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/food-items")
    public ResponseEntity<Page<FoodItemResponse>> getAllFoodItems(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "5", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice) {

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(Math.max(pageNo, 0), pageSize, sort);

        Page<FoodItemResponse> responses = foodItemService.getAllFoodItems(categoryId, search, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping({"/allCategoriws", "/allCategories"})
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        List<CategoryResponse> allCategories = categoryService.getAllCategories();
        ApiResponse<List<CategoryResponse>> apiResponse = ApiResponse.<List<CategoryResponse>>builder()
                .httpStatus(HttpStatus.OK)
                .message("Category fetched")
                .responseObject(allCategories)
                .build();
        return new ResponseEntity<>(apiResponse, apiResponse.getHttpStatus());


    }
}
