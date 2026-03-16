package com.dikshanta.restaurant.management.system.group_project.enums;

import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static com.dikshanta.restaurant.management.system.group_project.enums.Permission.*;

@AllArgsConstructor
public enum Role {

    CUSTOMER(Set.of(
            CUSTOMER_CREATE,
            CUSTOMER_READ,
            CUSTOMER_UPDATE,
            CUSTOMER_DELETE)),

    RESTAURANT(Set.of(
            Permission.RESTAURANT_CREATE,
            Permission.RESTAURANT_READ,
            Permission.RESTAURANT_UPDATE,
            Permission.RESTAURANT_DELETE
    )),
    ADMIN(Set.of(
            CUSTOMER_CREATE,
            CUSTOMER_READ,
            CUSTOMER_UPDATE,
            Permission.CUSTOMER_DELETE,
            Permission.RESTAURANT_CREATE,
            Permission.RESTAURANT_READ,
            Permission.RESTAURANT_UPDATE,
            Permission.RESTAURANT_DELETE,
            Permission.ADMIN_CREATE,
            Permission.ADMIN_READ,
            Permission.ADMIN_UPDATE,
            Permission.ADMIN_DELETE
    ));
    private final Set<Permission> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return permissions
                .stream()
                .map(permission -> new SimpleGrantedAuthority(STR."ROLE_\{this.name()}")).toList();

    }
}
