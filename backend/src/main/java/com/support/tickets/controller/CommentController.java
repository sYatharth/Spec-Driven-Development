package com.support.tickets.controller;

import com.support.tickets.dto.CommentResponse;
import com.support.tickets.dto.CreateCommentRequest;
import com.support.tickets.service.CommentService;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for adding comments to a ticket (FR-8).
 * Contract: spec/api-contract.md §3.
 */
@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Add a comment -> 201 / 400 / 404. */
    @PostMapping
    public ResponseEntity<CommentResponse> add(@PathVariable Long ticketId,
                                               @Valid @RequestBody CreateCommentRequest req) {
        CommentResponse created = commentService.addComment(ticketId, req);
        return ResponseEntity
                .created(URI.create("/api/tickets/" + ticketId + "/comments/" + created.id()))
                .body(created);
    }
}
