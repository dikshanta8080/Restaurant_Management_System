package com.dikshanta.restaurant.management.system.group_project.enums;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.dikshanta.restaurant.management.system.group_project.enums.Permission.*;

@AllArgsConstructor
public enum Role {

    CUSTOMER(Set.of(
            CUSTOMER_CREATE,
            CUSTOMER_READ,
            CUSTOMER_UPDATE,
            CUSTOMER_DELETE
    )),

    RESTAURANT(Set.of(
            RESTAURANT_CREATE,
            RESTAURANT_READ,
            RESTAURANT_UPDATE,
            RESTAURANT_DELETE
    )),

    ADMIN(Set.of(
            CUSTOMER_CREATE,
            CUSTOMER_READ,
            CUSTOMER_UPDATE,
            CUSTOMER_DELETE,
            RESTAURANT_CREATE,
            RESTAURANT_READ,
            RESTAURANT_UPDATE,
            RESTAURANT_DELETE,
            ADMIN_CREATE,
            ADMIN_READ,
            ADMIN_UPDATE,
            ADMIN_DELETE
    ));
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        permissions.forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()))
        );

        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;
    }
}
