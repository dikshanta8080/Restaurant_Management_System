package com.dikshanta.restaurant.management.system.group_project.exceptions;

import com.dikshanta.restaurant.management.system.group_project.model.ApiResponse;
import com.dikshanta.restaurant.management.system.group_project.model.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleRuntimeExceptions(Exception e) {
        HttpStatus httpStatus;
        httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .exceptionClass(e.getClass().getSimpleName())
                .exceptionMessage(e.getMessage())
                .errorMap(null)
                .exceptionTime(LocalDateTime.now())
                .build();
        ApiResponse<ExceptionResponse> apiResponse = ApiResponse.<ExceptionResponse>builder()
                .httpStatus(httpStatus)
                .message("Exception Occurred")
                .responseObject(exceptionResponse)
                .build();
        return new ResponseEntity<>(apiResponse, httpStatus);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ExceptionResponse>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        Map<String, String> errorMap = new HashMap<>();
        fieldErrors.forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        HttpStatus httpStatus;
        httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .exceptionClass(e.getClass().getSimpleName())
                .exceptionMessage(e.getMessage())
                .errorMap(errorMap)
                .exceptionTime(LocalDateTime.now())
                .build();
        ApiResponse<ExceptionResponse> apiResponse = ApiResponse.<ExceptionResponse>builder()
                .httpStatus(httpStatus)
                .message("Method Argument Not valid Exception Occurred")
                .responseObject(exceptionResponse)
                .build();
        return new ResponseEntity<>(apiResponse, httpStatus);
    }
}
