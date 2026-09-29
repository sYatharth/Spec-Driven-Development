package com.support.tickets.domain;

/**
 * Ticket lifecycle states.
 *
 * <p>Authoritative transition rules live in {@code spec/state-machine.md} and are
 * enforced by {@code StatusTransitionValidator}. Lifecycle is forward-only:
 * {@code CLOSED} and {@code CANCELLED} are terminal.
 */
public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
