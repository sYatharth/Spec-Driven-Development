package com.support.tickets.service;

import com.support.tickets.domain.Priority;
import com.support.tickets.domain.Ticket;
import com.support.tickets.domain.TicketStatus;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.dto.TicketDetailResponse;
import com.support.tickets.dto.TicketSummaryResponse;
import com.support.tickets.dto.UpdateTicketRequest;
import com.support.tickets.error.NotFoundException;
import com.support.tickets.repository.TicketRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for tickets (M4). Owns transactions and status-machine
 * enforcement (via {@link StatusTransitionValidator}). Controllers never touch
 * repositories or entities directly — only DTOs cross this boundary.
 */
@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final StatusTransitionValidator transitionValidator;

    public TicketService(TicketRepository ticketRepository,
                         StatusTransitionValidator transitionValidator) {
        this.ticketRepository = ticketRepository;
        this.transitionValidator = transitionValidator;
    }

    /** FR-1: create a ticket. Status is always OPEN; priority defaults to MEDIUM. */
    public TicketDetailResponse create(CreateTicketRequest req) {
        Priority priority = req.priority() != null ? req.priority() : Priority.MEDIUM;
        Ticket ticket = new Ticket(req.title(), req.description(), priority, req.assignee());
        return TicketDetailResponse.from(ticketRepository.save(ticket));
    }

    /**
     * FR-2/9/10: list tickets, optionally filtered by keyword and/or status.
     * Empty/blank keyword is treated as "no keyword filter" (Requirements Q2).
     */
    @Transactional(readOnly = true)
    public List<TicketSummaryResponse> list(String keyword, TicketStatus status) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        String q = hasKeyword ? keyword.trim() : null;

        List<Ticket> tickets;
        if (hasKeyword && status != null) {
            tickets = ticketRepository.searchByStatus(q, status);
        } else if (hasKeyword) {
            tickets = ticketRepository.search(q);
        } else if (status != null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtDescIdDesc(status);
        } else {
            tickets = ticketRepository.findAllByOrderByCreatedAtDescIdDesc();
        }
        return tickets.stream().map(TicketSummaryResponse::from).toList();
    }

    /** FR-3: full ticket detail incl. comments. */
    @Transactional(readOnly = true)
    public TicketDetailResponse getDetail(Long id) {
        return TicketDetailResponse.from(findOrThrow(id));
    }

    /** FR-4..7: partial field update. Null = leave unchanged; assignee "" = clear. */
    public TicketDetailResponse updateFields(Long id, UpdateTicketRequest req) {
        Ticket ticket = findOrThrow(id);

        if (req.title() != null) {
            ticket.setTitle(req.title());
        }
        if (req.description() != null) {
            ticket.setDescription(req.description());
        }
        if (req.priority() != null) {
            ticket.setPriority(req.priority());
        }
        if (req.assignee() != null) {
            // empty string clears the assignee; any other value sets it
            ticket.setAssignee(req.assignee().isBlank() ? null : req.assignee());
        }
        return TicketDetailResponse.from(ticket);
    }

    /** FR-11/12: change status, enforcing the state machine. */
    public TicketDetailResponse changeStatus(Long id, TicketStatus target) {
        Ticket ticket = findOrThrow(id);
        transitionValidator.assertCanTransition(ticket.getStatus(), target);
        ticket.setStatus(target);
        return TicketDetailResponse.from(ticket);
    }

    private Ticket findOrThrow(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> NotFoundException.ticket(id));
    }
}
