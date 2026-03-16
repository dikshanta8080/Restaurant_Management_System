package com.dikshanta.restaurant.management.system.group_project.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmailValidatorConstraint.class)
public @interface EmailValidator {
    String message() default "please provide valid email";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
