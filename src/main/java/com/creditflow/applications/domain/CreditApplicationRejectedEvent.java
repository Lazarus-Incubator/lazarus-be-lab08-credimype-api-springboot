package com.creditflow.applications.domain;

import com.creditflow.audit.domain.AuditableDomainEvent;
import java.time.Instant;
import java.util.Map;

public record CreditApplicationRejectedEvent(
        Long institutionId,
        Long actorUserId,
        String entityId,
        String previousStatus,
        String newStatus,
        Map<String, Object> detail,
        Instant occurredAt) implements AuditableDomainEvent {

    @Override
    public String action() {
        return "CREDIT_APPLICATION_REJECTED";
    }

    @Override
    public String entityType() {
        return "CREDIT_APPLICATION";
    }
}
