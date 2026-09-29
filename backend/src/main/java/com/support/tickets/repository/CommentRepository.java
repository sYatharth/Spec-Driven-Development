package com.support.tickets.repository;

import com.support.tickets.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Persistence for {@link Comment}. Comments are usually accessed via their
 * parent ticket, but this repository backs direct create operations (FR-8).
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
}
