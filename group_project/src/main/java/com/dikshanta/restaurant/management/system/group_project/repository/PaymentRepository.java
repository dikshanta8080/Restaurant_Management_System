package com.dikshanta.restaurant.management.system.group_project.repository;

import com.dikshanta.restaurant.management.system.group_project.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByUserId(Long userId);

    @Query("""
            SELECT DISTINCT p
            FROM Payment p
            JOIN p.order o
            JOIN o.orderItems oi
            WHERE oi.restaurant.owner.id = :ownerId
            """)
    List<Payment> findForRestaurantOwner(@Param("ownerId") Long ownerId);
}
