package com.support.tickets;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.support.tickets.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * End-to-end API tests covering CRUD, comments, search, filter, and validation
 * (FR-1,2,3,4-8,9,10,14; NFR-3). Uses an isolated in-memory H2 (test profile).
 */
@SpringBootTest
@AutoConfigureMockMvc
class TicketApiIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private TicketRepository ticketRepository;

    @BeforeEach
    void clean() {
        ticketRepository.deleteAll();
    }

    // ---- FR-1: create ----

    @Test
    @DisplayName("create ticket applies defaults OPEN/MEDIUM and returns 201")
    void create_applies_defaults() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"title":"Login broken","description":"Cannot log in"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status", is("OPEN")))
                .andExpect(jsonPath("$.priority", is("MEDIUM")))
                .andExpect(jsonPath("$.comments", hasSize(0)));
    }

    // ---- FR-14: validation ----

    @Test
    @DisplayName("blank title returns 400 with uniform error body and fieldErrors")
    void blank_title_returns_400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"title":"","description":"x"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Bad Request")))
                .andExpect(jsonPath("$.fieldErrors[?(@.field=='title')]", hasSize(1)));
    }

    @Test
    @DisplayName("invalid priority enum returns 400")
    void invalid_priority_returns_400() throws Exception {
        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"title":"t","description":"d","priority":"SUPER"}"""))
                .andExpect(status().isBadRequest());
    }

    // ---- FR-2: list newest first ----

    @Test
    @DisplayName("list returns tickets newest first")
    void list_newest_first() throws Exception {
        long first = createTicket("First", "alpha desc", null);
        long second = createTicket("Second", "beta desc", null);

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is((int) second)))
                .andExpect(jsonPath("$[1].id", is((int) first)));
    }

    // ---- FR-3: detail + 404 ----

    @Test
    @DisplayName("unknown ticket detail returns 404")
    void unknown_detail_returns_404() throws Exception {
        mockMvc.perform(get("/api/tickets/{id}", 424242))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---- FR-4..7: update fields ----

    @Test
    @DisplayName("partial update changes only provided fields; empty assignee clears it")
    void partial_update() throws Exception {
        long id = createTicket("Title", "Desc", "alice");

        // update only priority -> assignee must remain "alice"
        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"priority":"URGENT"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.priority", is("URGENT")))
                .andExpect(jsonPath("$.assignee", is("alice")));

        // clear assignee with empty string
        mockMvc.perform(patch("/api/tickets/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"assignee":""}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee", is(nullValue())));
    }

    // ---- FR-8: comments ----

    @Test
    @DisplayName("add comment returns 201; defaults author to agent; appears in detail")
    void add_comment() throws Exception {
        long id = createTicket("Title", "Desc", null);

        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"body":"Investigating"}"""))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.author", is("agent")))
                .andExpect(jsonPath("$.body", is("Investigating")));

        mockMvc.perform(get("/api/tickets/{id}", id))
                .andExpect(jsonPath("$.comments", hasSize(1)));
    }

    @Test
    @DisplayName("comment on unknown ticket returns 404; blank body returns 400")
    void comment_errors() throws Exception {
        mockMvc.perform(post("/api/tickets/{id}/comments", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"body":"x"}"""))
                .andExpect(status().isNotFound());

        long id = createTicket("Title", "Desc", null);
        mockMvc.perform(post("/api/tickets/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"body":""}"""))
                .andExpect(status().isBadRequest());
    }

    // ---- FR-9: search ----

    @Test
    @DisplayName("search matches title or description, case-insensitive")
    void search_keyword() throws Exception {
        createTicket("Login button broken", "Safari issue", null);
        createTicket("Export CSV", "fails on large files", null);

        mockMvc.perform(get("/api/tickets").param("q", "LOGIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", containsString("Login")));

        mockMvc.perform(get("/api/tickets").param("q", "large"))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Export CSV")));
    }

    // ---- FR-10: filter ----

    @Test
    @DisplayName("filter by status returns only matching; invalid status returns 400")
    void filter_by_status() throws Exception {
        long a = createTicket("A", "a", null);
        createTicket("B", "b", null);
        // move A to IN_PROGRESS
        mockMvc.perform(patch("/api/tickets/{id}/status", a)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"status":"IN_PROGRESS"}"""))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tickets").param("status", "IN_PROGRESS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) a)));

        mockMvc.perform(get("/api/tickets").param("status", "OPEN"))
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/tickets").param("status", "NOPE"))
                .andExpect(status().isBadRequest());
    }

    // ---- helper ----

    private long createTicket(String title, String desc, String assignee) throws Exception {
        String body = objectMapper.writeValueAsString(new java.util.LinkedHashMap<>() {{
            put("title", title);
            put("description", desc);
            if (assignee != null) {
                put("assignee", assignee);
            }
        }});
        var result = mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
