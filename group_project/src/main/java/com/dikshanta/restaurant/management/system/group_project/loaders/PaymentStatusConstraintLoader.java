package com.dikshanta.restaurant.management.system.group_project.loaders;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentStatusConstraintLoader implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("ALTER TABLE payments DROP CONSTRAINT IF EXISTS payments_status_check");
        jdbcTemplate.execute(
                "ALTER TABLE payments ADD CONSTRAINT payments_status_check " +
                        "CHECK (status IN ('PENDING','PAID','FAILED'))"
        );
    }
}
