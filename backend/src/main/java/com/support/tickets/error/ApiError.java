package com.support.tickets.error;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error response body (NFR-3, FR-15). Shape defined in
 * spec/api-contract.md §9. {@code fieldErrors} is empty for non-validation errors.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<FieldError> fieldErrors
) {
    public record FieldError(String field, String message) {
    }

    public static ApiError of(int status, String error, String message) {
        return new ApiError(Instant.now(), status, error, message, List.of());
    }

    public static ApiError of(int status, String error, String message, List<FieldError> fieldErrors) {
        return new ApiError(Instant.now(), status, error, message, fieldErrors);
    }
}
