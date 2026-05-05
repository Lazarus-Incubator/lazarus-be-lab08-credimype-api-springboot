package com.creditflow.shared.application.exception;

/**
 * Raised when an idempotency key is reused for an incompatible operation.
 */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
