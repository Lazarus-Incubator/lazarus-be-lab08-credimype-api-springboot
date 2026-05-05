package com.creditflow.shared.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Human-readable business identifier for credit applications.
 *
 * <p>The API uses this value object instead of exposing internal database ids in audit trails and
 * operational views because analysts and auditors reason about credit files by business number.</p>
 */
public final class ApplicationNumber implements Serializable {

    private static final Pattern PATTERN = Pattern.compile("CFM-\\d{4}-\\d{6}");

    private final String value;

    private ApplicationNumber(String value) {
        this.value = value;
    }

    public static ApplicationNumber of(String value) {
        Objects.requireNonNull(value, "Application number is required");
        if (!PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Application number must match CFM-YYYY-XXXXXX");
        }
        return new ApplicationNumber(value);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ApplicationNumber that)) {
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
