package com.dikshanta.restaurant.management.system.group_project.model.entities;

import com.dikshanta.restaurant.management.system.group_project.enums.PaymentMethod;
import com.dikshanta.restaurant.management.system.group_project.enums.PaymentStatus;
import com.dikshanta.restaurant.management.system.group_project.model.DateAuditable;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payments")
public class Payment extends DateAuditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method; // enum, no empty check

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING; // default value

    private String transactionId;

    @OneToOne
    @JoinColumn(name = "order_id", unique = true)
    private Order order;
}