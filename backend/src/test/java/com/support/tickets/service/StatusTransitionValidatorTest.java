package com.support.tickets.service;

import static com.support.tickets.domain.TicketStatus.CANCELLED;
import static com.support.tickets.domain.TicketStatus.CLOSED;
import static com.support.tickets.domain.TicketStatus.IN_PROGRESS;
import static com.support.tickets.domain.TicketStatus.OPEN;
import static com.support.tickets.domain.TicketStatus.RESOLVED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.support.tickets.domain.TicketStatus;
import com.support.tickets.error.InvalidStatusTransitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for the state machine (M3, NFR-5). Pure logic, no Spring context.
 * Mirrors the transition matrix in spec/state-machine.md.
 */
class StatusTransitionValidatorTest {

    private final StatusTransitionValidator validator = new StatusTransitionValidator();

    @ParameterizedTest(name = "[{index}] {0} -> {1} is allowed")
    @CsvSource({
            "OPEN, IN_PROGRESS",
            "OPEN, CANCELLED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CANCELLED",
            "RESOLVED, CLOSED"
    })
    @DisplayName("allowed transitions pass")
    void should_allow_valid_transitions(TicketStatus from, TicketStatus to) {
        assertThat(validator.isAllowed(from, to)).isTrue();
        // Should not throw.
        validator.assertCanTransition(from, to);
    }

    @ParameterizedTest(name = "[{index}] {0} -> {1} is rejected")
    @CsvSource({
            // Explicitly forbidden by the assignment
            "CLOSED, OPEN",
            "RESOLVED, OPEN",
            "CANCELLED, OPEN",
            // Skipping states
            "OPEN, RESOLVED",
            "OPEN, CLOSED",
            "IN_PROGRESS, CLOSED",
            // Terminal states have no outgoing transitions
            "RESOLVED, CANCELLED",
            "CLOSED, CANCELLED",
            "CANCELLED, IN_PROGRESS",
            // Self-transitions
            "OPEN, OPEN",
            "IN_PROGRESS, IN_PROGRESS",
            "CLOSED, CLOSED"
    })
    @DisplayName("invalid transitions are rejected with InvalidStatusTransitionException")
    void should_reject_invalid_transitions(TicketStatus from, TicketStatus to) {
        assertThat(validator.isAllowed(from, to)).isFalse();
        assertThatThrownBy(() -> validator.assertCanTransition(from, to))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining(from.name())
                .hasMessageContaining(to.name());
    }

    @ParameterizedTest(name = "terminal state {0} has no outgoing transitions")
    @CsvSource({ "CLOSED", "CANCELLED" })
    @DisplayName("terminal states reject every target")
    void terminal_states_have_no_outgoing(TicketStatus terminal) {
        for (TicketStatus target : TicketStatus.values()) {
            assertThat(validator.isAllowed(terminal, target))
                    .as("%s -> %s", terminal, target)
                    .isFalse();
        }
    }
}
