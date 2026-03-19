package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.dto.response.CategoryResponse;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Category;
import com.dikshanta.restaurant.management.system.group_project.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(category -> new CategoryResponse(category.getId(), category.getName()))
                .toList();
    }
}
