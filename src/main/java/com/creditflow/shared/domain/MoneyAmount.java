package com.creditflow.shared.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a monetary amount with a fixed scale of two decimals.
 *
 * <p>The value object protects the domain from raw {@link BigDecimal} handling spread across
 * aggregates. It normalizes scale, rejects nulls and non-positive values, and exposes only the
 * comparisons needed by credit rules.</p>
 */
public final class MoneyAmount implements Comparable<MoneyAmount>, Serializable {

    private final BigDecimal value;

    private MoneyAmount(BigDecimal value) {
        this.value = normalize(value);
    }

    public static MoneyAmount of(BigDecimal value) {
        BigDecimal normalized = normalize(value);
        if (normalized.signum() <= 0) {
            throw new IllegalArgumentException("Money amount must be greater than zero");
        }
        return new MoneyAmount(normalized);
    }

    public static MoneyAmount ofNullable(BigDecimal value) {
        return value == null ? null : of(value);
    }

    private static BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value, "Money amount is required");
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal toBigDecimal() {
        return value;
    }

    public boolean isGreaterThan(MoneyAmount other) {
        return compareTo(other) > 0;
    }

    public boolean isLessThan(MoneyAmount other) {
        return compareTo(other) < 0;
    }

    public boolean isGreaterThanOrEqualTo(MoneyAmount other) {
        return compareTo(other) >= 0;
    }

    @Override
    public int compareTo(MoneyAmount other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MoneyAmount that)) {
            return false;
        }
        return value.compareTo(that.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
