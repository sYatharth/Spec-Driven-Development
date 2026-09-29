package com.support.tickets.dto;

import com.support.tickets.domain.Comment;
import java.time.Instant;

/**
 * Response view of a comment (spec/api-contract.md §3).
 */
public record CommentResponse(
        Long id,
        Long ticketId,
        String author,
        String body,
        Instant createdAt
) {
    public static CommentResponse from(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getTicket() != null ? c.getTicket().getId() : null,
                c.getAuthor(),
                c.getBody(),
                c.getCreatedAt()
        );
    }
}
