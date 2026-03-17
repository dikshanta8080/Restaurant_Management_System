package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantStatusUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import com.dikshanta.restaurant.management.system.group_project.exceptions.RestaurantAlreadyExistsException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.RestaurantDoesNotExistsException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserDoesnotExistsException;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Address;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Restaurant;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.AddressRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.RestaurantRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final SecurityAuditorAware securityAuditorAware;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    public RestaurantCreateResponse createRestaurant(RestaurantCreateRequest request) {
        if (restaurantRepository.existsByName((request.getName()))) {
            throw new RestaurantAlreadyExistsException("Restaurant Already exists");

        }
        Long ownerId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new UserDoesnotExistsException("Owner does not exists"));
        Address address = Address.builder()
                .province(request.getProvince())
                .district(request.getDistrict())
                .city(request.getCity())
                .street(request.getStreet())
                .build();
        Address restaurantAddress = addressRepository.save(address);
        Restaurant newRestaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .address(restaurantAddress)
                .imageUrl("/uploads/dikshanta")
                .status(RestaurantStatus.PENDING)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);
        System.out.println(savedRestaurant);
        owner.getRestaurants().add(newRestaurant);
        return RestaurantCreateResponse.builder()
                .id(savedRestaurant.getId())
                .name(savedRestaurant.getName())
                .description(savedRestaurant.getDescription())
                .status(savedRestaurant.getStatus())
                .addressId(savedRestaurant.getAddress().getId())
                .ownerId(savedRestaurant.getOwner().getId())
                .imageUrl(savedRestaurant.getImageUrl())
                .build();


    }

    public void updateRestaurantStatus(RestaurantStatusUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId()).orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        restaurant.setStatus(request.getRestaurantStatus());
        restaurantRepository.save(restaurant);
    }

    public List<Restaurant> getRestaurants() {

        return restaurantRepository.findAll();
    }

    public Restaurant getOwnRestaurant() {
        Long ownerId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return restaurantRepository.findByOwner(ownerId)
                .orElseThrow(() -> new RestaurantDoesNotExistsException("You don't have any restaurant registered"));
    }
}
