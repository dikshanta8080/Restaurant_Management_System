package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.enums.Role;
import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Role role);
}
