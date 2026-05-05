package com.creditflow.shared.application.exception;

/**
 * Raised when a workflow command attempts to move an aggregate into a state that violates the
 * credit lifecycle.
 */
public class InvalidStateTransitionException extends RuntimeException {

    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
