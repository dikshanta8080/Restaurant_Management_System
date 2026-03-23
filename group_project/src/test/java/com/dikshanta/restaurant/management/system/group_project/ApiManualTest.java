package com.dikshanta.restaurant.management.system.group_project;

import com.dikshanta.restaurant.management.system.group_project.dto.request.FoodItemRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RegisterResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.FoodCategory;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Address;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Category;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class ApiManualTest {
    private static final String BASE_URL = "http://localhost:8090/api/v1";
    private static final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        System.out.println("Starting API Test...");
        
        // 1. Register User
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setName("Java Test User");
        registerRequest.setEmail("java" + System.currentTimeMillis() + "@test.com");
        registerRequest.setPassword("password");
        registerRequest.setProvince("Test Province");
        registerRequest.setDistrict("Test District");
        registerRequest.setCity("Test City");
        registerRequest.setStreet("Test Street");
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> registerEntity = new HttpEntity<>(registerRequest, headers);
        
        ResponseEntity<RegisterResponse> registerResponse = restTemplate.postForEntity(BASE_URL + "/auth/register", registerEntity, RegisterResponse.class);
        String token = registerResponse.getBody().getToken();
        System.out.println("Token acquired: " + token);

        // Build authenticated headers
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.setBearerAuth(token);

        // 2. Create Restaurant
        Restaurant restaurant = new Restaurant();
        restaurant.setName("Test Restaurant");
        restaurant.setDescription("Test Description");
        
        Address addr = new Address();
        addr.setProvince("Test Province");
        addr.setDistrict("Test District");
        addr.setCity("Test City");
        addr.setStreet("Test Street");
        restaurant.setAddress(addr);
        
        HttpEntity<Restaurant> restaurantEntity = new HttpEntity<>(restaurant, authHeaders);
        ResponseEntity<Restaurant> restaurantResponse = restTemplate.postForEntity(BASE_URL + "/customer/createRestaurant", restaurantEntity, Restaurant.class);
        Long restaurantId = restaurantResponse.getBody().getId();
        System.out.println("Restaurant created with ID: " + restaurantId);

        // 3. Create Category
        Category category = new Category();
        category.setName(FoodCategory.BEVERAGE);
        
        HttpEntity<Category> categoryEntity = new HttpEntity<>(category, authHeaders);
        ResponseEntity<Category> categoryResponse = restTemplate.postForEntity(BASE_URL + "/customer/categories", categoryEntity, Category.class);
        Long categoryId = categoryResponse.getBody().getId();
        System.out.println("Category created with ID: " + categoryId);

        // 4. Create Food Item
        FoodItemRequest foodRequest = new FoodItemRequest();
        foodRequest.setName("Java Test Food");
        foodRequest.setDescription("Java Test Desc");
        foodRequest.setPrice(new BigDecimal("12.99"));
        foodRequest.setAvailable(true);
        foodRequest.setCategoryId(categoryId);
        
        HttpEntity<FoodItemRequest> foodEntity = new HttpEntity<>(foodRequest, authHeaders);
        ResponseEntity<String> foodResponse = restTemplate.postForEntity(BASE_URL + "/restaurant/restaurants/" + restaurantId + "/food-items", foodEntity, String.class);
        System.out.println("Food Item Response: " + foodResponse.getBody());
        
        // 5. Get All Food Items
        HttpEntity<Void> getEntity = new HttpEntity<>(authHeaders);
        ResponseEntity<String> allItemsResponse = restTemplate.exchange(BASE_URL + "/restaurant/food-items?pageNo=1&pageSize=5", HttpMethod.GET, getEntity, String.class);
        System.out.println("Get All Items Response: " + allItemsResponse.getBody());
    }
}
