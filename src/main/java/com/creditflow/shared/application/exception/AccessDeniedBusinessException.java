package com.creditflow.shared.application.exception;

/**
 * Used by application-layer authorization policies when the authenticated actor is known but is not
 * allowed to perform the requested business action.
 */
public class AccessDeniedBusinessException extends RuntimeException {

    public AccessDeniedBusinessException(String message) {
        super(message);
    }
}
