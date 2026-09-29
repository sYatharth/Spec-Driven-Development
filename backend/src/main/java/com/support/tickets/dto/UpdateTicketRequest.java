package com.support.tickets.dto;

import com.support.tickets.domain.Priority;
import jakarta.validation.constraints.Size;

/**
 * Partial update of ticket fields (FR-4..7). Only non-null fields are applied.
 *
 * <p>Status is intentionally NOT updatable here — use the dedicated
 * {@code PATCH /tickets/{id}/status} endpoint so state-machine rules cannot be
 * bypassed (see spec/api-contract.md §2.4).
 *
 * <p>Update semantics (safe partial update): a {@code null} field means "leave
 * unchanged" for ALL fields — so a title-only update cannot accidentally wipe the
 * assignee. To explicitly clear the assignee, send an empty string {@code ""}.
 */
public record UpdateTicketRequest(

        @Size(min = 1, max = 150, message = "must be between 1 and 150 characters")
        String title,

        @Size(min = 1, max = 5000, message = "must be between 1 and 5000 characters")
        String description,

        Priority priority,

        @Size(max = 100, message = "must be at most 100 characters")
        String assignee
) {
}
