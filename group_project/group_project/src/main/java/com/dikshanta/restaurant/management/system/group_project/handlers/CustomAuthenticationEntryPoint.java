package com.dikshanta.restaurant.management.system.group_project.handlers;

import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ExceptionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .exceptionClass(authException.getClass().getSimpleName())
                .exceptionMessage(authException.getMessage())
                .errorMap(null)
                .exceptionTime(LocalDateTime.now())
                .build();

        ApiResponse<ExceptionResponse> apiResponse = ApiResponse.<ExceptionResponse>builder()
                .httpStatus(httpStatus)
                .message("Authentication Failed")
                .responseObject(exceptionResponse)
                .build();

        objectMapper.writeValue(response.getOutputStream(), apiResponse);
    }
}