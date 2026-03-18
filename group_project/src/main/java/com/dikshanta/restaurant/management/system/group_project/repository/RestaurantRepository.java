package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Optional<Restaurant> findByName(String name);

    boolean existsByName(String name);

    Optional<Restaurant> findByOwner(Long owner);

    boolean existsByOwner(User owner);
}
