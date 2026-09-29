package com.support.tickets.dto;

import com.support.tickets.domain.Priority;
import com.support.tickets.domain.Ticket;
import com.support.tickets.domain.TicketStatus;
import java.time.Instant;

/**
 * Lightweight ticket view for list responses (FR-2). Excludes description and
 * comments to keep list payloads small (spec/api-contract.md §2.2).
 */
public record TicketSummaryResponse(
        Long id,
        String title,
        TicketStatus status,
        Priority priority,
        String assignee,
        Instant updatedAt
) {
    public static TicketSummaryResponse from(Ticket t) {
        return new TicketSummaryResponse(
                t.getId(),
                t.getTitle(),
                t.getStatus(),
                t.getPriority(),
                t.getAssignee(),
                t.getUpdatedAt()
        );
    }
}
