package com.dikshanta.restaurant.management.system.group_project.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UnauthorizedReviewAccessException extends RuntimeException {
    public UnauthorizedReviewAccessException(String message) {
        super(message);
    }
}
