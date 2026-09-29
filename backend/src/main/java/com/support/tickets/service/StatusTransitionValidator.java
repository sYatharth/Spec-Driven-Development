package com.support.tickets.service;

import static com.support.tickets.domain.TicketStatus.CANCELLED;
import static com.support.tickets.domain.TicketStatus.CLOSED;
import static com.support.tickets.domain.TicketStatus.IN_PROGRESS;
import static com.support.tickets.domain.TicketStatus.OPEN;
import static com.support.tickets.domain.TicketStatus.RESOLVED;

import com.support.tickets.domain.TicketStatus;
import com.support.tickets.error.InvalidStatusTransitionException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Single source of truth for the ticket lifecycle state machine
 * (spec/state-machine.md). Forward-only: CLOSED and CANCELLED are terminal.
 *
 * <p>Allowed transitions:
 * <pre>
 *   OPEN         -> IN_PROGRESS, CANCELLED
 *   IN_PROGRESS  -> RESOLVED, CANCELLED
 *   RESOLVED     -> CLOSED
 *   CLOSED       -> (none)
 *   CANCELLED    -> (none)
 * </pre>
 * Self-transitions (X -> X) are rejected.
 */
@Component
public class StatusTransitionValidator {

    private static final Map<TicketStatus, Set<TicketStatus>> ALLOWED;

    static {
        Map<TicketStatus, Set<TicketStatus>> m = new EnumMap<>(TicketStatus.class);
        m.put(OPEN, EnumSet.of(IN_PROGRESS, CANCELLED));
        m.put(IN_PROGRESS, EnumSet.of(RESOLVED, CANCELLED));
        m.put(RESOLVED, EnumSet.of(CLOSED));
        m.put(CLOSED, EnumSet.noneOf(TicketStatus.class));
        m.put(CANCELLED, EnumSet.noneOf(TicketStatus.class));
        ALLOWED = m;
    }

    /** @return true if the transition from {@code from} to {@code to} is permitted. */
    public boolean isAllowed(TicketStatus from, TicketStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    /**
     * Enforces the state machine.
     *
     * @throws InvalidStatusTransitionException (mapped to 409) if not allowed.
     */
    public void assertCanTransition(TicketStatus from, TicketStatus to) {
        if (!isAllowed(from, to)) {
            throw new InvalidStatusTransitionException(from, to);
        }
    }
}
