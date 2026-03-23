package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.FoodItemRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.FoodItemResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.exceptions.FoodItemNotFoundException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.RestaurantDoesNotExistsException;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Category;
import com.dikshanta.restaurant.management.system.group_project.model.entities.FoodItem;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import com.dikshanta.restaurant.management.system.group_project.repository.CategoryRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.FoodItemRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.RestaurantRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FoodItemService {
    private final FileUploadService fileUploadService;

    private final FoodItemRepository foodItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final CategoryRepository categoryRepository;
    private final SecurityAuditorAware securityAuditorAware;
    private final UserRepository userRepository;

    private FoodItemResponse mapToResponse(FoodItem foodItem) {
        return FoodItemResponse.builder()
                .id(foodItem.getId())
                .name(foodItem.getName())
                .description(foodItem.getDescription())
                .price(foodItem.getPrice())
                .available(foodItem.getAvailable())
                .imageUrl(foodItem.getImageUrl())
                .categoryId(foodItem.getCategory() != null ? foodItem.getCategory().getId() : null)
                .restaurantId(foodItem.getRestaurant() != null ? foodItem.getRestaurant().getId() : null)
                .build();
    }

    private void validateRestaurantOwner(Restaurant restaurant) {
        Long ownerId = securityAuditorAware.getCurrentAuditor().orElseThrow(() -> new RuntimeException("User not authenticated"));
        boolean isOwner = restaurant.getOwner().getId().equals(ownerId);
        boolean isAdmin = userRepository.findById(ownerId)
                .map(user -> user.getRole() == Role.ADMIN)
                .orElse(false);
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("You are not authorized to perform this action for this restaurant");
        }
    }

    @Transactional
    public FoodItemResponse addFoodItem(Long restaurantId, FoodItemRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId).orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        validateRestaurantOwner(restaurant);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
        }
        String imageUrl = null;
        if (request.getMultipartFile() != null && !request.getMultipartFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.getMultipartFile(), "foodItem");
        }
        FoodItem newFoodItem = FoodItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .available(request.getAvailable() != null ? request.getAvailable() : true)
                .imageUrl(imageUrl)
                .restaurant(restaurant)
                .category(category)
                .build();

        FoodItem savedFoodItem = foodItemRepository.save(newFoodItem);
        return mapToResponse(savedFoodItem);
    }

    @Transactional
    public FoodItemResponse updateFoodItem(Long id, FoodItemRequest request) {
        FoodItem foodItem = foodItemRepository.findById(id).orElseThrow(() -> new FoodItemNotFoundException("Food item not found"));
        validateRestaurantOwner(foodItem.getRestaurant());

        if (request.getName() != null) {
            foodItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            foodItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            foodItem.setPrice(request.getPrice());
        }
        if (request.getAvailable() != null) {
            foodItem.setAvailable(request.getAvailable());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId()).orElseThrow(() -> new RuntimeException("Category not found"));
            foodItem.setCategory(category);
        }

        FoodItem updatedFoodItem = foodItemRepository.save(foodItem);
        return mapToResponse(updatedFoodItem);
    }

    @Transactional
    public void deleteFoodItem(Long id) {
        FoodItem foodItem = foodItemRepository.findById(id).orElseThrow(() -> new FoodItemNotFoundException("Food item not found"));
        validateRestaurantOwner(foodItem.getRestaurant());
        foodItemRepository.delete(foodItem);
    }

    public List<FoodItemResponse> getFoodItemsByRestaurant(Long restaurantId) {
        restaurantRepository.findById(restaurantId).orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        List<FoodItem> foodItems = foodItemRepository.findByRestaurantId(restaurantId);
        return foodItems.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public Page<FoodItemResponse> getAllFoodItems(Long categoryId, String search, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Page<FoodItem> foodItemPage = foodItemRepository.findByFilters(categoryId, search, minPrice, maxPrice, pageable);
        return foodItemPage.map(this::mapToResponse);
    }
}
