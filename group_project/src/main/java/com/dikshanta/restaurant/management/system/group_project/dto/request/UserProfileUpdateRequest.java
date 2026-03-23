package com.dikshanta.restaurant.management.system.group_project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileUpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;
    private String province;
    private String district;
    private String city;
    private String street;
    @Schema(type = "string", format = "binary")
    private MultipartFile multipartFile;

}
