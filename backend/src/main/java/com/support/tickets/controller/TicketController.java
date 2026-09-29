package com.support.tickets.controller;

import com.support.tickets.domain.TicketStatus;
import com.support.tickets.dto.ChangeStatusRequest;
import com.support.tickets.dto.CreateTicketRequest;
import com.support.tickets.dto.TicketDetailResponse;
import com.support.tickets.dto.TicketSummaryResponse;
import com.support.tickets.dto.UpdateTicketRequest;
import com.support.tickets.service.TicketService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for tickets. HTTP concerns only — all logic lives in
 * {@link TicketService}. Contract: spec/api-contract.md.
 */
@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /** FR-1: create ticket -> 201. */
    @PostMapping
    public ResponseEntity<TicketDetailResponse> create(@Valid @RequestBody CreateTicketRequest req) {
        TicketDetailResponse created = ticketService.create(req);
        return ResponseEntity
                .created(URI.create("/api/tickets/" + created.id()))
                .body(created);
    }

    /** FR-2/9/10: list, optionally filtered by keyword (q) and/or status. */
    @GetMapping
    public List<TicketSummaryResponse> list(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "status", required = false) TicketStatus status) {
        return ticketService.list(q, status);
    }

    /** FR-3: ticket detail -> 200 / 404. */
    @GetMapping("/{id}")
    public TicketDetailResponse detail(@PathVariable Long id) {
        return ticketService.getDetail(id);
    }

    /** FR-4..7: partial field update -> 200 / 400 / 404. Does not change status. */
    @PatchMapping("/{id}")
    public TicketDetailResponse update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateTicketRequest req) {
        return ticketService.updateFields(id, req);
    }

    /** FR-11/12: change status -> 200 / 400 / 404 / 409. */
    @PatchMapping("/{id}/status")
    public TicketDetailResponse changeStatus(@PathVariable Long id,
                                             @Valid @RequestBody ChangeStatusRequest req) {
        return ticketService.changeStatus(id, req.status());
    }
}
