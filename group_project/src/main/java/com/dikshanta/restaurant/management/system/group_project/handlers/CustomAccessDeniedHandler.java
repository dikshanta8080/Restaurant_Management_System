package com.dikshanta.restaurant.management.system.group_project.handlers;

import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        HttpStatus httpStatus = HttpStatus.FORBIDDEN;

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .exceptionClass(accessDeniedException.getClass().getSimpleName())
                .exceptionMessage(accessDeniedException.getMessage())
                .errorMap(null)
                .exceptionTime(LocalDateTime.now())
                .build();

        ApiResponse<ExceptionResponse> apiResponse = ApiResponse.<ExceptionResponse>builder()
                .httpStatus(httpStatus)
                .message("Access Denied")
                .responseObject(exceptionResponse)
                .build();

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}