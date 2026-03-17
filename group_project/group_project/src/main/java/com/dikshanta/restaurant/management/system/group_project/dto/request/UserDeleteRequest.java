package com.dikshanta.restaurant.management.system.group_project.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDeleteRequest {
    @NotNull(message = "Id can not be null")
    private Long id;
}
