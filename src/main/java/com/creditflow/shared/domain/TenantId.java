package com.creditflow.shared.domain;

/**
 * Lightweight identifier for institution scoping rules.
 *
 * <p>Separating tenant ids from raw longs makes authorization code more self-documenting and reduces
 * the risk of mixing institution and branch identifiers in policy checks.</p>
 */
public record TenantId(Long value) {

    public TenantId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("Tenant id must be a positive number");
        }
    }
}
