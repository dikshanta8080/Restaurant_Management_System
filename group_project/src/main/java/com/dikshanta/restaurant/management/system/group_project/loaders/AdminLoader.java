package com.dikshanta.restaurant.management.system.group_project.loaders;

import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import com.dikshanta.restaurant.management.system.group_project.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class AdminLoader implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final Utils utils;

    @Override
    public void run(String... args) throws Exception {
        Utils.Admin adminData = utils.getAdmin();
        if (!userRepository.existsByEmail(adminData.getEmail())) {
            User admin = User.builder()
                    .name(adminData.getName())
                    .email(adminData.getEmail())
                    .role(Role.ADMIN)
                    .profileImageUrl(adminData.getProfileImagePath())
                    .password(passwordEncoder.encode(adminData.getPassword()))
                    .build();
            userRepository.save(admin);
        }

    }
}
