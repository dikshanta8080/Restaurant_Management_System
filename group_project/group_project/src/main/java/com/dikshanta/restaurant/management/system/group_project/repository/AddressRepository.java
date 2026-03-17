package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
}
