package com.dikshanta.restaurant.management.system.group_project.model.entities;


import com.dikshanta.restaurant.management.system.group_project.enums.FoodCategory;
import com.dikshanta.restaurant.management.system.group_project.model.EditorAuditable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends EditorAuditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private FoodCategory name;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<FoodItem> foodItems = new ArrayList<>();

    public void addFoodItem(FoodItem foodItem) {
        foodItems.add(foodItem);
        foodItem.setCategory(this);
    }

    public void removeFoodItem(FoodItem foodItem) {
        foodItems.remove(foodItem);
        foodItem.setCategory(null);
    }
}