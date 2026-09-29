package com.support.tickets.error;

/**
 * Thrown when a requested resource (ticket/comment) does not exist. Mapped to
 * HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException ticket(Long id) {
        return new NotFoundException("Ticket not found: " + id);
    }
}
