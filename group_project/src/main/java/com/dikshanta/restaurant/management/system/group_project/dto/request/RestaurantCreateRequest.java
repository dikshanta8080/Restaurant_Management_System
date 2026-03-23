package com.dikshanta.restaurant.management.system.group_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RestaurantCreateRequest {
    private String name;
    private String description;
    private String province;
    private String district;
    private String city;
    private String street;
    @Schema(type = "string", format = "binary")
    private MultipartFile multipartFile;


}