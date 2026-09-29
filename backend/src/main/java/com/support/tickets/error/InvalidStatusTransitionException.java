package com.support.tickets.error;

import com.support.tickets.domain.TicketStatus;

/**
 * Thrown when a status change violates the state machine (spec/state-machine.md).
 * Mapped to HTTP 409 Conflict by {@link GlobalExceptionHandler}.
 */
public class InvalidStatusTransitionException extends RuntimeException {

    private final TicketStatus from;
    private final TicketStatus to;

    public InvalidStatusTransitionException(TicketStatus from, TicketStatus to) {
        super("Cannot transition ticket from " + from + " to " + to);
        this.from = from;
        this.to = to;
    }

    public TicketStatus getFrom() {
        return from;
    }

    public TicketStatus getTo() {
        return to;
    }
}
