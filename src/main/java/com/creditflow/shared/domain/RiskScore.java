package com.creditflow.shared.domain;

import java.io.Serializable;

/**
 * Bounded risk score used by the credit risk workflow.
 *
 * <p>The score stays within the inclusive 0..100 range so risk decisions cannot persist impossible
 * values that would later break approval and reporting logic.</p>
 */
public final class RiskScore implements Serializable {

    private final int value;

    private RiskScore(int value) {
        this.value = value;
    }

    public static RiskScore of(int value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
        return new RiskScore(value);
    }

    public int value() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RiskScore that && value == that.value;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
