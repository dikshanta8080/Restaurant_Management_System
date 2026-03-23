package com.dikshanta.restaurant.management.system.group_project.loaders;

import com.dikshanta.restaurant.management.system.group_project.enums.FoodCategory;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Category;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.CategoryRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import com.dikshanta.restaurant.management.system.group_project.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class AdminLoader implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Utils utils;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        Utils.Admin adminData = utils.getAdmin();
        User admin = userRepository.findByEmail(adminData.getEmail())
                .orElse(User.builder().email(adminData.getEmail()).build());
        admin.setName(adminData.getName());
        admin.setRole(Role.ADMIN);
        admin.setProfileImageUrl(adminData.getProfileImagePath());
        admin.setPassword(passwordEncoder.encode(adminData.getPassword()));
        userRepository.save(admin);
        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            List<Category> defaultCategories = List.of(
                    Category.builder().name(FoodCategory.VEG).build(),
                    Category.builder().name(FoodCategory.DRINKS).build(),
                    Category.builder().name(FoodCategory.NONVEG).build(),
                    Category.builder().name(FoodCategory.FASTFOODS).build()
            );
            categoryRepository.saveAll(defaultCategories);
        }

    }
}
