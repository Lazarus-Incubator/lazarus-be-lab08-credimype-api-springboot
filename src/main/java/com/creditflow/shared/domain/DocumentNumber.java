package com.creditflow.shared.domain;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulates borrower document numbers to avoid passing raw strings through the credit domain.
 *
 * <p>Normalization happens once at creation time so repository constraints, search filters and
 * audit records all use the same canonical representation.</p>
 */
public final class DocumentNumber implements Serializable {

    private final String value;

    private DocumentNumber(String value) {
        this.value = value;
    }

    public static DocumentNumber of(String rawValue) {
        Objects.requireNonNull(rawValue, "Document number is required");
        String normalized = rawValue.trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be blank");
        }
        if (normalized.length() < 8 || normalized.length() > 20) {
            throw new IllegalArgumentException("Document number length must be between 8 and 20 characters");
        }
        return new DocumentNumber(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DocumentNumber that)) {
            return false;
        }
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
