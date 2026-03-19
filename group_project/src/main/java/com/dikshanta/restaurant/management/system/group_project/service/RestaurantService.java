package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantCreateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RestaurantStatusUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantCreateResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RestaurantResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.RestaurantStatus;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.exceptions.RestaurantAlreadyExistsException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.RestaurantDoesNotExistsException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UnauthorizedReviewAccessException;
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


    @Transactional
    public RestaurantCreateResponse createRestaurant(RestaurantCreateRequest request) {
        if (restaurantRepository.existsByName(request.getName())) {
            throw new RestaurantAlreadyExistsException("Restaurant already exists with that name");
        }

        User owner = getCurrentUser();

        if (restaurantRepository.existsByOwner(owner)) {
            throw new RestaurantAlreadyExistsException("You already have a registered restaurant");
        }

        String imageUrl = null;
        if (request.getMultipartFile() != null && !request.getMultipartFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.getMultipartFile(), "restaurant");
        }
        Address address = addressRepository.save(Address.builder()
                .province(request.getProvince())
                .district(request.getDistrict())
                .city(request.getCity())
                .street(request.getStreet())
                .build());

        Restaurant newRestaurant = Restaurant.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .address(address)
                .imageUrl(imageUrl)
                .status(RestaurantStatus.PENDING)
                .build();

        Restaurant saved = restaurantRepository.save(newRestaurant);

        owner.setRestaurant(saved);
        userRepository.save(owner);

        return toCreateResponse(saved);
    }

    public List<RestaurantResponse> getPendingRestaurants() {
        return restaurantRepository.findAllByStatus(RestaurantStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<RestaurantResponse> getAllRestaurantsForAdmin() {
        return restaurantRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void updateRestaurantStatus(RestaurantStatusUpdateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(request.getRestaurantId())
                .orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));
        restaurant.setStatus(request.getRestaurantStatus());
        restaurantRepository.save(restaurant);
    }


    public List<RestaurantResponse> getRestaurants() {
        return restaurantRepository.findAllByStatus(RestaurantStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── Owner's own restaurant ───────────────────────────────────────────────


    public RestaurantResponse getOwnRestaurant() {
        User currentUser = getCurrentUser();
        Restaurant restaurant = restaurantRepository.findByOwner(currentUser.getId())
                .orElseThrow(() -> new RestaurantDoesNotExistsException(
                        "You don't have any restaurant registered"));
        return toResponse(restaurant);
    }

    @Transactional
    public RestaurantCreateResponse updateRestaurant(Long id, RestaurantCreateRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));

        User currentUser = getCurrentUser();
        validateOwnerOrAdmin(restaurant, currentUser, "update");

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

        if (request.getMultipartFile() != null && !request.getMultipartFile().isEmpty()) {
            String imageUrl = fileUploadService.uploadFile(request.getMultipartFile(), "restaurant");
            restaurant.setImageUrl(imageUrl);
        }

        return toCreateResponse(restaurantRepository.save(restaurant));
    }


    @Transactional
    public void deleteRestaurant(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));

        User currentUser = getCurrentUser();
        validateOwnerOrAdmin(restaurant, currentUser, "delete");

        restaurantRepository.delete(restaurant);
    }


    private User getCurrentUser() {
        Long userId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDoesnotExistsException("User not found"));
    }

    private void validateOwnerOrAdmin(Restaurant restaurant, User currentUser, String operation) {
        boolean isOwner = restaurant.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedReviewAccessException(
                    "You are not authorized to " + operation + " this restaurant");
        }
    }


    private RestaurantResponse toResponse(Restaurant restaurant) {
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .imageUrl(restaurant.getImageUrl())
                .ownerName(restaurant.getOwner().getName())
                .status(restaurant.getStatus())   // ← was missing
                .build();
    }

    private RestaurantCreateResponse toCreateResponse(Restaurant restaurant) {
        return RestaurantCreateResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .status(restaurant.getStatus())
                .addressId(restaurant.getAddress().getId())
                .ownerId(restaurant.getOwner().getId())
                .imageUrl(restaurant.getImageUrl())
                .build();
    }
}