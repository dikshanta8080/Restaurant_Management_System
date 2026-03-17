package com.dikshanta.restaurant.management.system.group_project.model;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private HttpStatus httpStatus;
    private String message;
    private T responseObject;
}
