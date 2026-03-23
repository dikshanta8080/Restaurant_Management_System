package com.dikshanta.restaurant.management.system.group_project.service;

import com.dikshanta.restaurant.management.system.group_project.dto.request.LoginRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.request.RegisterRequest;
import com.dikshanta.restaurant.management.system.group_project.dto.response.LoginResponse;
import com.dikshanta.restaurant.management.system.group_project.dto.response.RegisterResponse;
import com.dikshanta.restaurant.management.system.group_project.exceptions.UserDoesnotExistsException;
import com.dikshanta.restaurant.management.system.group_project.mappers.response.RegisterResponseMapper;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtService jwtService;
    private final RegisterResponseMapper responseMapper;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;


    public RegisterResponse registerUser(RegisterRequest registerRequest) {
        User user = userService.registerUser(registerRequest);
        RegisterResponse response = responseMapper.apply(user);
        String token = jwtService.getJwt(user);
        response.setToken(token);
        return response;
    }

    public LoginResponse authenticateUser(LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new UserDoesnotExistsException("User with the provided credentials does not exists"));
            String jwt = jwtService.getJwt(user);
            return LoginResponse.builder()
                    .token(jwt)
                    .user(responseMapper.apply(user))
                    .build();
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Please provide valid username and password to login");
        }
    }
}
