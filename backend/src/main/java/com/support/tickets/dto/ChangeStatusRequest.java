package com.support.tickets.dto;

import com.support.tickets.domain.TicketStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for a status change (FR-11/12).
 *
 * <p>An unknown enum value fails JSON deserialization and is mapped to 400 by
 * the global handler; a valid-but-disallowed transition is rejected with 409 by
 * the service via {@code StatusTransitionValidator}.
 */
public record ChangeStatusRequest(

        @NotNull(message = "must not be null")
        TicketStatus status
) {
}
