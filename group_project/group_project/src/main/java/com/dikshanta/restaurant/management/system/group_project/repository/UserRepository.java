package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    Optional<User> findByEmail(String username);

    boolean existsByEmail(String email);

}
