package com.dikshanta.restaurant.management.system.group_project;

import com.dikshanta.restaurant.management.system.group_project.service.RestaurantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GroupProjectApplicationTests {
    @Autowired
    private RestaurantService restaurantService;

    @Test
    @Disabled("Manual smoke test; requires seeded database and is not suitable for CI.")
    void contextLoads() {
        restaurantService.getRestaurants(null, null, "name", "asc")
                .forEach(restaurant -> System.out.println(restaurant));
    }

}
