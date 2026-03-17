package com.dikshanta.restaurant.management.system.group_project.configurations;

import com.dikshanta.restaurant.management.system.group_project.model.UserPrincipal;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal userPrincipal)) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal1) {
            return Optional.ofNullable(userPrincipal.getId());
        }
        return Optional.empty();
    }
}
