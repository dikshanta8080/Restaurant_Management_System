package com.dikshanta.restaurant.management.system.group_project.controller;

import com.dikshanta.restaurant.management.system.group_project.dto.request.ReviewRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.ReviewResponse;
import com.dikshanta.restaurant.management.system.group_project.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(@Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.createReview(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReviewResponse> updateReview(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        ReviewResponse response = reviewService.updateReview(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ReviewResponse> responses = reviewService.getReviewsForRestaurant(restaurantId, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/food-items/{foodItemId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForFoodItem(
            @PathVariable Long foodItemId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ReviewResponse> responses = reviewService.getReviewsForFoodItem(foodItemId, page, size);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<ReviewResponse> responses = reviewService.getUserReviews(userId, page, size);
        return ResponseEntity.ok(responses);
    }
}
