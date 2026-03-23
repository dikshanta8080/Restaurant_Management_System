package com.dikshanta.restaurant.management.system.group_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FoodItemRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean available;
    private Long categoryId;
    @Schema(type = "string", format = "binary")
    private MultipartFile multipartFile;
}
