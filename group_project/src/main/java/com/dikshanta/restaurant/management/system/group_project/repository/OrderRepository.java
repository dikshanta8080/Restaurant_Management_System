package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByOrderItemsRestaurantId(Long restaurantId, Pageable pageable);
    Page<Order> findDistinctByOrderItemsRestaurantOwnerId(Long ownerId, Pageable pageable);
    void deleteAllByUserId(Long userId);
}
