package com.support.tickets.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.support.tickets.domain.Priority;
import com.support.tickets.domain.Ticket;
import com.support.tickets.domain.TicketStatus;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/**
 * Repository slice tests for search (FR-9) and status filter (FR-10) queries.
 */
@DataJpaTest
class TicketRepositoryTest {

    @Autowired
    private TicketRepository repository;

    @BeforeEach
    void seed() {
        repository.deleteAll();
        repository.save(new Ticket("Login button broken", "Fails on Safari", Priority.HIGH, "alice"));
        repository.save(new Ticket("Export CSV", "Large file export fails", Priority.MEDIUM, "bob"));
        Ticket inProgress = new Ticket("Login timeout", "Session drops", Priority.LOW, null);
        inProgress.setStatus(TicketStatus.IN_PROGRESS);
        repository.save(inProgress);
    }

    @Test
    @DisplayName("search is case-insensitive and matches title or description")
    void search_matches_title_or_description() {
        List<Ticket> byTitle = repository.search("login");
        assertThat(byTitle).extracting(Ticket::getTitle)
                .containsExactlyInAnyOrder("Login button broken", "Login timeout");

        List<Ticket> byDescription = repository.search("EXPORT");
        assertThat(byDescription).hasSize(1);
        assertThat(byDescription.get(0).getTitle()).isEqualTo("Export CSV");
    }

    @Test
    @DisplayName("filter by status returns only matching tickets")
    void filter_by_status() {
        assertThat(repository.findByStatusOrderByCreatedAtDescIdDesc(TicketStatus.OPEN)).hasSize(2);
        assertThat(repository.findByStatusOrderByCreatedAtDescIdDesc(TicketStatus.IN_PROGRESS)).hasSize(1);
        assertThat(repository.findByStatusOrderByCreatedAtDescIdDesc(TicketStatus.CLOSED)).isEmpty();
    }

    @Test
    @DisplayName("search combined with status filter")
    void search_by_status() {
        List<Ticket> result = repository.searchByStatus("login", TicketStatus.IN_PROGRESS);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Login timeout");
    }

    @Test
    @DisplayName("list is ordered newest first (createdAt desc, id desc tiebreaker)")
    void list_newest_first() {
        List<Ticket> all = repository.findAllByOrderByCreatedAtDescIdDesc();
        assertThat(all).hasSize(3);
        // last saved should be first; id-desc tiebreaker keeps this deterministic
        assertThat(all.get(0).getTitle()).isEqualTo("Login timeout");
    }
}
