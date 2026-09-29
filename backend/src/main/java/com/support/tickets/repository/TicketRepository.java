package com.support.tickets.repository;

import com.support.tickets.domain.Ticket;
import com.support.tickets.domain.TicketStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Persistence for {@link Ticket}.
 *
 * <p>Supports listing (newest first), keyword search (FR-9, case-insensitive on
 * title OR description) and status filtering (FR-10), independently or combined.
 */
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // id DESC is a deterministic tiebreaker so ordering is stable even when
    // multiple tickets share the same createdAt timestamp (avoids flaky ordering).
    List<Ticket> findAllByOrderByCreatedAtDescIdDesc();

    List<Ticket> findByStatusOrderByCreatedAtDescIdDesc(TicketStatus status);

    /** Keyword search across title and description (case-insensitive). */
    @Query("""
           SELECT t FROM Ticket t
           WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
              OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%'))
           ORDER BY t.createdAt DESC, t.id DESC
           """)
    List<Ticket> search(@Param("q") String keyword);

    /** Keyword search combined with a status filter. */
    @Query("""
           SELECT t FROM Ticket t
           WHERE t.status = :status
             AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
                  OR LOWER(t.description) LIKE LOWER(CONCAT('%', :q, '%')))
           ORDER BY t.createdAt DESC, t.id DESC
           """)
    List<Ticket> searchByStatus(@Param("q") String keyword, @Param("status") TicketStatus status);
}
