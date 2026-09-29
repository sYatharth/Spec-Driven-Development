package com.support.tickets.domain;

/**
 * Ticket priority. Default on create is {@link #MEDIUM} (see spec/data-model.md).
 */
public enum Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}
