package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.ReviewRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.ReviewResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.exceptions.*;
import com.dikshanta.restaurant.management.system.group_project.model.entities.FoodItem;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Review;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.FoodItemRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.RestaurantRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.ReviewRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final FoodItemRepository foodItemRepository;
    private final SecurityAuditorAware securityAuditorAware;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        validateReviewRequest(request);

        User currentUser = getCurrentUser();


        if (currentUser.getRole() != Role.CUSTOMER) {
            throw new OnlyCustomerException("Only customers are allowed to submit reviews");
        }

        Review review = Review.builder()
                .rating(request.getRating())
                .comment(request.getComment())
                .user(currentUser)
                .build();

        if (request.getRestaurantId() != null) {
            Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                    .orElseThrow(() -> new RestaurantDoesNotExistsException(
                            "Restaurant not found with id: " + request.getRestaurantId()));
            review.setRestaurant(restaurant);
        } else if (request.getFoodItemId() != null) {
            FoodItem foodItem = foodItemRepository.findById(request.getFoodItemId())
                    .orElseThrow(() -> new FoodItemNotFoundException(
                            "Food item not found with id: " + request.getFoodItemId()));
            review.setFoodItem(foodItem);
        }

        Review savedReview = reviewRepository.save(review);
        return mapToReviewResponse(savedReview);
    }

    @Transactional
    public ReviewResponse updateReview(Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        validateOwnerOnly(review);

        if (request.getRating() != null) {
            if (request.getRating() < 1 || request.getRating() > 5) {
                throw new InvalidReviewException("Rating must be between 1 and 5");
            }
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }

        Review updatedReview = reviewRepository.save(review);
        return mapToReviewResponse(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found with id: " + reviewId));

        validateOwnerOrAdmin(review);

        reviewRepository.delete(review);
    }

    public List<ReviewResponse> getReviewsForRestaurant(Long restaurantId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByRestaurantId(restaurantId, pageable);
        return reviews.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsForFoodItem(Long foodItemId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByFoodItemId(foodItemId, pageable);
        return reviews.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getUserReviews(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.getContent().stream()
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());
    }


    private User getCurrentUser() {
        Long userId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDoesnotExistsException("User not found"));
    }

    private void validateReviewRequest(ReviewRequest request) {
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new InvalidReviewException("Rating must be between 1 and 5");
        }
        if (request.getRestaurantId() != null && request.getFoodItemId() != null) {
            throw new InvalidReviewException(
                    "Review cannot be for both restaurant and food item simultaneously");
        }
        if (request.getRestaurantId() == null && request.getFoodItemId() == null) {
            throw new InvalidReviewException(
                    "Review must be associated with either a restaurant or a food item");
        }
    }


    private void validateOwnerOnly(Review review) {
        User currentUser = getCurrentUser();
        if (!review.getUser().getId().equals(currentUser.getId())) {
            throw new UnauthorizedReviewAccessException(
                    "You are not authorized to modify this review");
        }
    }


    private void validateOwnerOrAdmin(Review review) {
        User currentUser = getCurrentUser();
        boolean isOwner = review.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedReviewAccessException(
                    "You are not authorized to delete this review");
        }
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .rating(review.getRating())
                .comment(review.getComment())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : null)
                .restaurantId(review.getRestaurant() != null ? review.getRestaurant().getId() : null)
                .foodItemId(review.getFoodItem() != null ? review.getFoodItem().getId() : null)
                .createdAt(review.getCreatedAt())
                .build();
    }
}
