package com.dikshanta.restaurant.management.system.group_project.security;

import com.dikshanta.restaurant.management.system.group_project.model.User;
import com.dikshanta.restaurant.management.system.group_project.model.UserPrincipal;
import com.dikshanta.restaurant.management.system.group_project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User with this username does not exists"));
        return new UserPrincipal(user.getId(), user.getEmail(), user.getPassword(), user.getRole().getAuthorities());
    }
}
