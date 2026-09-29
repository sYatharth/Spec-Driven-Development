package com.support.tickets.service;

import com.support.tickets.domain.Comment;
import com.support.tickets.domain.Ticket;
import com.support.tickets.dto.CommentResponse;
import com.support.tickets.dto.CreateCommentRequest;
import com.support.tickets.error.NotFoundException;
import com.support.tickets.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for comments (FR-8). A comment can only be added to an existing
 * ticket (404 otherwise). Author defaults to "agent" (handled in {@link Comment}).
 */
@Service
@Transactional
public class CommentService {

    private final TicketRepository ticketRepository;

    public CommentService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public CommentResponse addComment(Long ticketId, CreateCommentRequest req) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.ticket(ticketId));

        Comment comment = new Comment(req.body(), req.author());
        ticket.addComment(comment);          // maintains both sides + cascades on save
        // Ticket is managed within this transaction; cascade persists the comment.
        ticketRepository.save(ticket);
        return CommentResponse.from(comment);
    }
}
