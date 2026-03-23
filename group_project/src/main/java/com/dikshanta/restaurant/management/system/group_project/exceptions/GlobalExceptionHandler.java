package com.dikshanta.restaurant.management.system.group_project.exceptions;

import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        Map<String, String> errorMap = new HashMap<>();
        // Frontend can display this under the `email` field.
        errorMap.put("email", e.getMessage());
        return buildExceptionResponse(
                HttpStatus.CONFLICT,
                "User already exists",
                e.getMessage(),
                errorMap
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleBadCredentialsException(BadCredentialsException e) {
        Map<String, String> errorMap = new HashMap<>();
        // Login form should display this under the `password` field.
        errorMap.put("password", e.getMessage());
        return buildExceptionResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                e.getMessage(),
                errorMap
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleRuntimeExceptions(Exception e) {
        String exceptionMessage = e.getMessage() != null ? e.getMessage() : "Request failed";
        return buildExceptionResponse(
                HttpStatus.BAD_REQUEST,
                exceptionMessage,
                exceptionMessage,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errorMap = new HashMap<>();
        fieldErrors.forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return buildExceptionResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                "Validation failed",
                errorMap
        );
    }

    private ResponseEntity<ApiResponse<ExceptionResponse>> buildExceptionResponse(
            HttpStatus status,
            String apiMessage,
            String exceptionMessage,
            Map<String, String> errorMap
    ) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .exceptionClass(ExceptionResponse.class.getSimpleName())
                .exceptionMessage(exceptionMessage)
                .errorMap(errorMap)
                .exceptionTime(LocalDateTime.now())
                .build();

        ApiResponse<ExceptionResponse> apiResponse = ApiResponse.<ExceptionResponse>builder()
                .httpStatus(status)
                .message(apiMessage)
                .responseObject(exceptionResponse)
                .build();

        return new ResponseEntity<>(apiResponse, status);
    }
}
