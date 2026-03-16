package com.dikshanta.restaurant.management.system.group_project.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExceptionResponse {
    private String exceptionClass;
    private String exceptionMessage;
    private Map<String, String> errorMap;
    private LocalDateTime exceptionTime;
}
