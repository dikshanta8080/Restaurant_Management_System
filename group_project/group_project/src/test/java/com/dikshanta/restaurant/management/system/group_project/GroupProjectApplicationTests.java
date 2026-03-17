package com.dikshanta.restaurant.management.system.group_project;

import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GroupProjectApplicationTests {
    @Autowired
    private RestaurantService restaurantService;

    @Test
    void contextLoads() {
        restaurantService.getRestaurants().forEach(restaurant -> System.out.println(restaurant));
    }

}
