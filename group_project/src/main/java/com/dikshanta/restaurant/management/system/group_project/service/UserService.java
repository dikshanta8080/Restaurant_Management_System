package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.configurations.SecurityAuditorAware;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserDeleteRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserProfileUpdateRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.UserResponse;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserAlreadyExistsException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserDoesnotExistsException;
import com.dikshanta.restaurant.management.system.group_project.mappers.request.RegisterRequestMapper;
import com.dikshanta.restaurant.management.system.group_project.model.entities.Address;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.AddressRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.OrderRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.PaymentRepository;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RegisterRequestMapper registerRequestMapper;
    private final SecurityAuditorAware securityAuditorAware;
    private final AddressRepository addressRepository;
    private final FileUploadService fileUploadService;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = registerRequestMapper.apply(registerRequest);
        user.setPassword(encodedPassword);
        user.setRole(Role.CUSTOMER);
        user.setProfileImageUrl("uploads/user/dummy_profile.png");
        return userRepository.save(user);

    }

    @Transactional
    public void deleteUser(UserDeleteRequest userDeleteRequest) {
        if (!userRepository.existsById(userDeleteRequest.getId())) {
            throw new UserDoesnotExistsException("User does not exist");
        }
        Long userId = userDeleteRequest.getId();


        paymentRepository.deleteByOrderUserId(userId);
        orderRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    public List<UserResponse> getCustomers() {
        return userRepository.findAllByRole(Role.CUSTOMER)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    private User getCurrentUser() {
        Long userId = securityAuditorAware.getCurrentAuditor()
                .orElseThrow(() -> new RuntimeException("User not authenticated"));
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserDoesnotExistsException("User not found"));
    }

    public UserResponse getUserProfile() {
        User user = getCurrentUser();
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUserProfile(UserProfileUpdateRequest request) {
        User user = getCurrentUser();

        user.setName(request.getName());
        String imageUrl = null;
        if (request.getMultipartFile() != null && !request.getMultipartFile().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.getMultipartFile(), "user");
            user.setProfileImageUrl(imageUrl);
        }

        Address address = user.getAddress();
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
        user.setAddress(address);


        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        Long addressId = user.getAddress() != null ? user.getAddress().getId() : null;
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .profileImageUrl(user.getProfileImageUrl())
                .addressId(addressId)
                .province(user.getAddress() != null ? user.getAddress().getProvince() : null)
                .district(user.getAddress() != null ? user.getAddress().getDistrict() : null)
                .city(user.getAddress() != null ? user.getAddress().getCity() : null)
                .street(user.getAddress() != null ? user.getAddress().getStreet() : null)
                .build();
    }
}
