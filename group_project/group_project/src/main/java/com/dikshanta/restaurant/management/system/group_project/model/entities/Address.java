package com.dikshanta.restaurant.management.system.group_project.model.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String province;
    private String district;
    private String city;
    private String street;
    @OneToOne(mappedBy = "address")
    private User user;
    @OneToOne(mappedBy = "address")
    private Restaurant restaurant;
}