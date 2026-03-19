package com.dikshanta.restaurant.management.system.group_project.dto.response;

import com.dikshanta.restaurant.management.system.group_project.enums.FoodCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Data
@Getter
@Service
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private FoodCategory categoryName;
}
