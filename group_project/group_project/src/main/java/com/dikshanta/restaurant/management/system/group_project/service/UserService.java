package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.UserDeleteRequest;
import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.exceptions.OnlyCustomerException;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserAlreadyExistsException;
import com.dikshanta.restaurant.management.system.group_project.mappers.request.RegisterRequestMapper;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RegisterRequestMapper registerRequestMapper;

    @Transactional
    public User registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("User with this email already exists");
        }
        if (registerRequest.getRole() == Role.ADMIN) {
            throw new OnlyCustomerException("Sorry,you can only signup as customer");
        }
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = registerRequestMapper.apply(registerRequest);
        user.setPassword(encodedPassword);
        user.setProfileImageUrl("This is dummy profile url");
        return userRepository.save(user);

    }

    public void deleteUser(UserDeleteRequest userDeleteRequest) {
        userRepository.deleteById(userDeleteRequest.getId());
    }

}
