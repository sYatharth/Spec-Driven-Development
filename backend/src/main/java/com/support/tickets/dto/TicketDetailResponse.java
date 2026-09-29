package com.support.tickets.dto;

import com.support.tickets.domain.Priority;
import com.support.tickets.domain.Ticket;
import com.support.tickets.domain.TicketStatus;
import java.time.Instant;
import java.util.List;

/**
 * Full ticket view including comments (FR-3, spec/api-contract.md §2.3).
 */
public record TicketDetailResponse(
        Long id,
        String title,
        String description,
        TicketStatus status,
        Priority priority,
        String assignee,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> comments
) {
    public static TicketDetailResponse from(Ticket t) {
        List<CommentResponse> comments = t.getComments().stream()
                .map(CommentResponse::from)
                .toList();
        return new TicketDetailResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getAssignee(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                comments
        );
    }
}
