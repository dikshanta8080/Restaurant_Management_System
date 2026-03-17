package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByRestaurantId(Long restaurantId, Pageable pageable);
    
    Page<Review> findByFoodItemId(Long foodItemId, Pageable pageable);
    
    Page<Review> findByUserId(Long userId, Pageable pageable);
    
    List<Review> findByRestaurantId(Long restaurantId);
    
    List<Review> findByFoodItemId(Long foodItemId);
    
    List<Review> findByUserId(Long userId);
}
