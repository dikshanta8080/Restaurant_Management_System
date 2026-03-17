package com.dikshanta.restaurant.management.system.group_project.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {
    @GetMapping("/check")
    public ResponseEntity<String> getHealthStatus(HttpServletRequest request) {
        String response = STR."Tomcat is running in 8090 with session id \{request.getSession().getId()}";
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
