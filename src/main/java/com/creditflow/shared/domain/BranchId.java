package com.creditflow.shared.domain;

/**
 * Lightweight identifier for branch-specific access restrictions.
 */
public record BranchId(Long value) {

    public BranchId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Branch id must be a positive number");
        }
    }
}
