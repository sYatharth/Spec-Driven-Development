package com.support.tickets;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the Spring application context wires up (all beans,
 * controllers, services, repositories, config) without errors.
 */
@SpringBootTest
class TicketApplicationTests {

    @Test
    void contextLoads() {
        // Fails if any bean cannot be created / wiring is broken.
    }
}
