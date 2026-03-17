package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantStatusUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantResponse;
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
import jakarta.transaction.Transactional;
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
    private final FileUploadService fileUploadService;

    public RestaurantCreateResponse createRestaurant(RestaurantCreateRequest request) {
        if (restaurantRepository.existsByName((request.getName()))) {
            throw new RestaurantAlreadyExistsException("Restaurant Already exists");

        }
        Long ownerId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        User owner = userRepository.findById(ownerId).orElseThrow(() -> new UserDoesnotExistsException("Owner does not exists"));
        String url = fileUploadService.uploadFile(request.getMultipartFile(), "restaurant");
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
                .imageUrl(url)
                .status(RestaurantStatus.PENDING)
                .build();
        Restaurant savedRestaurant = restaurantRepository.save(newRestaurant);
        System.out.println(savedRestaurant);
        owner.setRestaurant(newRestaurant);
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

    public List<RestaurantResponse> getRestaurants() {

        List<Restaurant> restaurantList = restaurantRepository.findAll();
        return restaurantList.stream().map(restaurant -> {
            return RestaurantResponse.builder().
                    id(restaurant.getId())
                    .name(restaurant.getName())
                    .description(restaurant.getDescription())
                    .imageUrl(restaurant.getImageUrl())
                    .ownerName(restaurant.getOwner().getName())
                    .build();

        }).toList();

    }

    public RestaurantResponse getOwnRestaurant() {
        Long ownerId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        Restaurant restaurant = restaurantRepository.findByOwner(ownerId)
                .orElseThrow(() -> new RestaurantDoesNotExistsException("You don't have any restaurant registered"));
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .imageUrl(restaurant.getImageUrl())
                .ownerName(restaurant.getOwner().getName())
                .build();
    }

    @Transactional
    public RestaurantCreateResponse updateRestaurant(Long id, RestaurantCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        Long ownerId = securityAuditorAware.getCurrentAuditor().orElseThrow(() -> new RuntimeException("User not authenticated"));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You are not authorized to update this restaurant");
        }

        restaurant.setName(request.getName());
        restaurant.setDescription(request.getDescription());

        Address address = restaurant.getAddress();
        if (address == null) {
            address = new Address();
        }
        address.setProvince(request.getProvince());
        address.setDistrict(request.getDistrict());
        address.setCity(request.getCity());
        address.setStreet(request.getStreet());
        if (address.getId() == null) {
            address = addressRepository.save(address);
        }
        restaurant.setAddress(address);

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return RestaurantCreateResponse.builder()
                .id(updatedRestaurant.getId())
                .name(updatedRestaurant.getName())
                .description(updatedRestaurant.getDescription())
                .status(updatedRestaurant.getStatus())
                .addressId(updatedRestaurant.getAddress().getId())
                .ownerId(updatedRestaurant.getOwner().getId())
                .imageUrl(updatedRestaurant.getImageUrl())
                .build();
    }

    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        Long ownerId = securityAuditorAware.getCurrentAuditor().orElseThrow(() -> new RuntimeException("User not authenticated"));
        if (!restaurant.getOwner().getId().equals(ownerId)) {
            throw new RuntimeException("You are not authorized to delete this restaurant");
        }
        restaurantRepository.delete(restaurant);
    }
}
