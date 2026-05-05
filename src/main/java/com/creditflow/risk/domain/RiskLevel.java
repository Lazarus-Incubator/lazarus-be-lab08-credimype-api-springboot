package com.creditflow.risk.domain;

import com.creditflow.shared.domain.RiskScore;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    public static RiskLevel fromScore(RiskScore score) {
        int value = score.value();
        if (value >= 75) {
            return LOW;
        }
        if (value >= 50) {
            return MEDIUM;
        }
        if (value >= 30) {
            return HIGH;
        }
        return CRITICAL;
    }
}
