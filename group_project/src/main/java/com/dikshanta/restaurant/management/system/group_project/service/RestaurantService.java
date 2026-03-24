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
import com.dikshanta.restaurant.management.system.group_project.model.entities.Review;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.AddressRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.RestaurantRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.ReviewRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final SecurityAuditorAware securityAuditorAware;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final FileUploadService fileUploadService;
    private final ReviewRepository reviewRepository;


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

        // Auto-elevate / demote the owner's role based on approval status
        User owner = restaurant.getOwner();
        if (request.getRestaurantStatus() == RestaurantStatus.APPROVED) {
            owner.setRole(Role.RESTAURANT);
        } else {
            // REJECTED or PENDING → revert to CUSTOMER if they have no other approved restaurant
            boolean hasOtherApproved = restaurantRepository
                    .findAllByStatus(RestaurantStatus.APPROVED)
                    .stream()
                    .anyMatch(r -> r.getOwner().getId().equals(owner.getId())
                                  && !r.getId().equals(restaurant.getId()));
            if (!hasOtherApproved) {
                owner.setRole(Role.CUSTOMER);
            }
        }
        userRepository.save(owner);
    }


    public List<RestaurantResponse> getRestaurants(String search, Double minRating, String sortBy, String sortDir) {
        List<RestaurantResponse> responses = restaurantRepository.findAllByStatus(RestaurantStatus.APPROVED)
                .stream()
                .map(this::toResponse)
                .toList();

        if (search != null && !search.isBlank()) {
            String normalized = search.toLowerCase(Locale.ROOT);
            responses = responses.stream()
                    .filter(r -> (r.getName() != null && r.getName().toLowerCase(Locale.ROOT).contains(normalized))
                            || (r.getDescription() != null && r.getDescription().toLowerCase(Locale.ROOT).contains(normalized))
                            || (r.getOwnerName() != null && r.getOwnerName().toLowerCase(Locale.ROOT).contains(normalized)))
                    .toList();
        }

        if (minRating != null) {
            responses = responses.stream()
                    .filter(r -> r.getAverageRating() != null && r.getAverageRating() >= minRating)
                    .toList();
        }

        String field = (sortBy == null || sortBy.isBlank()) ? "name" : sortBy.toLowerCase(Locale.ROOT);
        boolean desc = "desc".equalsIgnoreCase(sortDir);
        responses = responses.stream().sorted((a, b) -> {
            int result;
            if ("rating".equals(field)) {
                result = Double.compare(a.getAverageRating() == null ? 0.0 : a.getAverageRating(),
                        b.getAverageRating() == null ? 0.0 : b.getAverageRating());
            } else {
                result = String.valueOf(a.getName()).compareToIgnoreCase(String.valueOf(b.getName()));
            }
            return desc ? -result : result;
        }).toList();
        return responses;
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

    @Transactional
    public void deleteRestaurantByAdmin(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantDoesNotExistsException("Restaurant not found"));

        User owner = restaurant.getOwner();
        if (owner != null) {
            owner.setRestaurant(null);
            if (owner.getRole() == Role.RESTAURANT) {
                boolean hasOtherApproved = restaurantRepository
                        .findAllByStatus(RestaurantStatus.APPROVED)
                        .stream()
                        .anyMatch(r -> r.getOwner() != null
                                && r.getOwner().getId().equals(owner.getId())
                                && !r.getId().equals(restaurant.getId()));
                if (!hasOtherApproved) {
                    owner.setRole(Role.CUSTOMER);
                }
            }
            userRepository.save(owner);
        }

        // Cascades remove food items/order items/reviews based on entity mappings.
        restaurantRepository.delete(restaurant);
    }


    private User getCurrentUser() {
        Long userId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDoesnotExistsException("User not found"));
    }

    private void validateOwnerOrAdmin(Restaurant restaurant, User currentUser, String operation) {
        // Seed/demo restaurants may have no owner; only admins can manage them.
        if (restaurant.getOwner() == null) {
            if (currentUser.getRole() != Role.ADMIN) {
                throw new UnauthorizedReviewAccessException(
                        "You are not authorized to " + operation + " this restaurant");
            }
            return;
        }

        boolean isOwner = restaurant.getOwner().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new UnauthorizedReviewAccessException(
                    "You are not authorized to " + operation + " this restaurant");
        }
    }


    private RestaurantResponse toResponse(Restaurant restaurant) {
        List<Review> reviews = reviewRepository.findByRestaurantId(restaurant.getId());
        double average = reviews.isEmpty()
                ? 0.0
                : reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        var address = restaurant.getAddress();
        String ownerName = restaurant.getOwner() != null ? restaurant.getOwner().getName() : null;
        return RestaurantResponse.builder()
                .id(restaurant.getId())
                .name(restaurant.getName())
                .description(restaurant.getDescription())
                .imageUrl(restaurant.getImageUrl())
                .ownerName(ownerName)
                .status(restaurant.getStatus())
                .averageRating(Math.round(average * 10.0) / 10.0)
                .reviewCount(reviews.size())
                .addressId(address != null ? address.getId() : null)
                .province(address != null ? address.getProvince() : null)
                .district(address != null ? address.getDistrict() : null)
                .city(address != null ? address.getCity() : null)
                .street(address != null ? address.getStreet() : null)
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