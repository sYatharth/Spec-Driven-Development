package com.support.tickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Support Ticket Management System backend.
 *
 * <p>Spec-Driven Development: see {@code spec/} for the authoritative design and
 * {@code plan/tasks.md} for the implementation plan.
 */
@SpringBootApplication
public class TicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(TicketApplication.class, args);
    }
}
