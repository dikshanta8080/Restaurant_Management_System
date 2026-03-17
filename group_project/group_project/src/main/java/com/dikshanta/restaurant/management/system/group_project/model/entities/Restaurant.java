package com.dikshanta.restaurant.management.system.group_project.model.entities;

import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import com.dikshanta.restaurant.management.system.group_project.model.EditorAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant extends EditorAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private RestaurantStatus status = RestaurantStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FoodItem> foodItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    public void addFoodItem(FoodItem foodItem) {
        foodItems.add(foodItem);
        foodItem.setRestaurant(this);
    }

    public void removeFoodItem(FoodItem foodItem) {
        foodItems.remove(foodItem);
        foodItem.setRestaurant(null);
    }
}