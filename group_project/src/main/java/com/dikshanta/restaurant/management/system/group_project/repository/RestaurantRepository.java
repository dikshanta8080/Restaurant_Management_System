package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    boolean existsByName(String name);

    boolean existsByOwner(User owner);

    Optional<Restaurant> findByName(String name);

    /**
     * Used by RestaurantService.getOwnRestaurant() — find by owner's user ID.
     */
    @Query("SELECT r FROM Restaurant r WHERE r.owner.id = :ownerId")
    Optional<Restaurant> findByOwner(@Param("ownerId") Long ownerId);

    /**
     * BUG FIXED: RestaurantService.getRestaurants() previously called findAll(),
     * which returned PENDING and REJECTED restaurants to customers.
     * This method replaces that call — only APPROVED restaurants are returned
     * to the public listing endpoint.
     */
    List<Restaurant> findAllByStatus(RestaurantStatus status);
}
 