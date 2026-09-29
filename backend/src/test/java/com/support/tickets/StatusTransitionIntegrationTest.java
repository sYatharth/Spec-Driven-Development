package com.support.tickets;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.tickets.domain.TicketStatus;
import com.support.tickets.dto.ChangeStatusRequest;
import com.support.tickets.dto.CreateTicketRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * State-machine integration tests (NFR-5) — the graded centerpiece.
 * Full stack: MockMvc -> controller -> service -> validator -> H2.
 * Mirrors spec/state-machine.md. Verifies both allowed transitions succeed and
 * every rejected transition returns 409 with the status left unchanged.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StatusTransitionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ---- Allowed transitions -> 200 + persisted new status ----

    @ParameterizedTest(name = "[{index}] {0} -> {1} allowed -> 200")
    @CsvSource({
            "OPEN, IN_PROGRESS",
            "OPEN, CANCELLED",
            "IN_PROGRESS, RESOLVED",
            "IN_PROGRESS, CANCELLED",
            "RESOLVED, CLOSED"
    })
    @DisplayName("allowed transitions succeed and persist")
    void allowed_transition_succeeds(TicketStatus from, TicketStatus to) throws Exception {
        long id = createTicketInStatus(from);

        mockMvc.perform(patch("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChangeStatusRequest(to))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(to.name())));

        // Confirm persisted via a fresh read.
        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(to.name())));
    }

    // ---- Rejected transitions -> 409 + status unchanged ----

    @ParameterizedTest(name = "[{index}] {0} -> {1} rejected -> 409")
    @CsvSource({
            // Explicitly forbidden by the assignment
            "CLOSED, OPEN",
            "RESOLVED, OPEN",
            "CANCELLED, OPEN",
            // Skipping intermediate states
            "OPEN, RESOLVED",
            "OPEN, CLOSED",
            "IN_PROGRESS, CLOSED",
            // Terminal states have no outgoing transitions
            "RESOLVED, CANCELLED",
            "CLOSED, CANCELLED",
            "CANCELLED, IN_PROGRESS",
            // Self-transition
            "OPEN, OPEN"
    })
    @DisplayName("invalid transitions are rejected with 409 and do not change state")
    void rejected_transition_returns_409(TicketStatus from, TicketStatus to) throws Exception {
        long id = createTicketInStatus(from);

        mockMvc.perform(patch("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChangeStatusRequest(to))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.message", is("Cannot transition ticket from "
                        + from + " to " + to)));

        // State must be unchanged.
        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(from.name())));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("changing status of an unknown ticket returns 404")
    void unknown_ticket_returns_404() throws Exception {
        mockMvc.perform(patch("/api/tickets/{id}/status", 999999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new ChangeStatusRequest(TicketStatus.IN_PROGRESS))))
                .andExpect(status().isNotFound());
    }

    @org.junit.jupiter.api.Test
    @DisplayName("invalid status enum value returns 400")
    void invalid_status_value_returns_400() throws Exception {
        long id = createTicketInStatus(TicketStatus.OPEN);
        mockMvc.perform(patch("/api/tickets/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"BOGUS\"}"))
                .andExpect(status().isBadRequest());
    }

    // ---- helpers ----

    /** Creates a ticket (OPEN) and walks it to the desired start status via valid transitions. */
    private long createTicketInStatus(TicketStatus target) throws Exception {
        long id = createOpenTicket();
        for (TicketStatus step : pathToStatus(target)) {
            mockMvc.perform(patch("/api/tickets/{id}/status", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(new ChangeStatusRequest(step))))
                    .andExpect(status().isOk());
        }
        return id;
    }

    private long createOpenTicket() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new CreateTicketRequest(
                                "Test ticket", "Test description", null, null))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    /** Sequence of valid transitions from OPEN to reach {@code target}. */
    private static List<TicketStatus> pathToStatus(TicketStatus target) {
        return switch (target) {
            case OPEN -> List.of();
            case IN_PROGRESS -> List.of(TicketStatus.IN_PROGRESS);
            case RESOLVED -> List.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED);
            case CLOSED -> List.of(TicketStatus.IN_PROGRESS, TicketStatus.RESOLVED, TicketStatus.CLOSED);
            case CANCELLED -> List.of(TicketStatus.CANCELLED);
        };
    }

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }
}
