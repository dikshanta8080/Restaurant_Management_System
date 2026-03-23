package com.dikshanta.restaurant.management.system.group_project.loaders;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderStatusConstraintLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE orders DROP CONSTRAINT IF EXISTS orders_status_check");
        jdbcTemplate.execute("ALTER TABLE order_items DROP CONSTRAINT IF EXISTS order_items_status_check");

        String allowedStatuses = "('PENDING','ACCEPTED','COMPLETED','REJECTED')";
        jdbcTemplate.execute(
                "ALTER TABLE orders ADD CONSTRAINT orders_status_check CHECK (status IN " + allowedStatuses + ")"
        );
        jdbcTemplate.execute(
                "ALTER TABLE order_items ADD CONSTRAINT order_items_status_check CHECK (status IN " + allowedStatuses + ")"
        );
    }
}
