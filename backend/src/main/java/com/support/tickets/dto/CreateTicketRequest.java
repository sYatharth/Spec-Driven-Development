package com.support.tickets.dto;

import com.support.tickets.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a ticket (FR-1). Validation per spec/data-model.md.
 *
 * <p>{@code priority} is optional (defaults to MEDIUM); {@code assignee} is optional.
 * Status is never client-supplied on create — it is always OPEN.
 */
public record CreateTicketRequest(

        @NotBlank(message = "must not be blank")
        @Size(max = 150, message = "must be at most 150 characters")
        String title,

        @NotBlank(message = "must not be blank")
        @Size(max = 5000, message = "must be at most 5000 characters")
        String description,

        Priority priority,

        @Size(max = 100, message = "must be at most 100 characters")
        String assignee
) {
}
