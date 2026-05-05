package com.creditflow.shared.application.exception;

/**
 * Signals that an invariant or business rule rejected a request even though the payload was
 * syntactically valid.
 */
public class DomainRuleViolationException extends RuntimeException {

    public DomainRuleViolationException(String message) {
        super(message);
    }
}
