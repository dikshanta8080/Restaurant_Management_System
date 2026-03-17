package com.dikshanta.restaurant.management.system.group_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class GroupProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(GroupProjectApplication.class, args);
    }

}
