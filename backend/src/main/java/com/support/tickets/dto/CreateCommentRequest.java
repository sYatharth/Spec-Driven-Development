package com.support.tickets.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for adding a comment (FR-8).
 *
 * <p>{@code author} is optional and defaults to "agent" (v1 has no auth).
 */
public record CreateCommentRequest(

        @NotBlank(message = "must not be blank")
        @Size(max = 2000, message = "must be at most 2000 characters")
        String body,

        @Size(max = 100, message = "must be at most 100 characters")
        String author
) {
}
